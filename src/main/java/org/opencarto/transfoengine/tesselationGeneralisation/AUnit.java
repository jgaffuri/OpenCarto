/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Agent;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A tesselation unit, which consists of one or several ADomains.
 * It is an agent representing a multipolygon statistical unit.
 * 
 * @author julien Gaffuri
 *
 */
public class AUnit extends Agent {

	public AUnit(Feature f) {
		super(f);
		this.setId(f.id);
		aDomains = new HashSet<ADomain>();
	}

	public Feature getObject() { return (Feature)super.getObject(); }


	//the patches composing the units
	public Collection<ADomain> aDomains;

	//update unit geometry from domain geometries
	public void updateGeomFromDomainGeoms(){
		MultiPolygon mp = new GeometryFactory().createMultiPolygon(new Polygon[]{});
		for(ADomain aDom : aDomains) {
			if(aDom.isDeleted()) continue;
			Geometry aDomGeom = aDom.getObject().getGeometry();
			if(aDomGeom==null) continue;
			mp = (MultiPolygon) JTSGeomUtil.toMulti( mp.union(aDomGeom) );
		}
		getObject().setGeom(mp);
	}

	public int getNumberOfNonDeletedDomains() {
		int n=0;
		for(ADomain aDom : aDomains) if(!aDom.isDeleted()) n++;
		return n;
	}

}
