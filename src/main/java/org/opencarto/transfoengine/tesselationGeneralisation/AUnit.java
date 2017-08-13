/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Agent;
import org.opencarto.util.JTSGeomUtil;

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
		for(ADomain adom : aDomains) {
			if(adom.isDeleted()) continue;
			mp = (MultiPolygon) JTSGeomUtil.toMulti( mp.union(adom.getObject().getGeometry()) );
		}
		getObject().setGeom(mp);
	}

}
