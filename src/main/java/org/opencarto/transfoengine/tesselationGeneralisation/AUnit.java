/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.algo.base.Union;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Agent;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A tesselation unit, which consists of one or several AFaces.
 * It is an agent representing a multipolygon statistical unit.
 * 
 * @author julien Gaffuri
 *
 */
public class AUnit extends Agent {

	public AUnit(Feature f) {
		super(f);
		this.setId(f.id);
		aFaces = new HashSet<AFace>();
		straits = new HashSet<Polygon>();
	}

	public Feature getObject() { return (Feature)super.getObject(); }

	//the patches composing the units
	public Collection<AFace> aFaces;

	//the straits
	public Collection<Polygon> straits;

	//update unit geometry from face geometries
	public void updateGeomFromFaceGeoms(){
		MultiPolygon mp = new GeometryFactory().createMultiPolygon(new Polygon[]{});
		for(AFace aFace : aFaces) {
			if(aFace.isDeleted()) continue;
			Geometry aFaceGeom = aFace.getObject().getGeometry();
			//if(aFaceGeom==null) continue;
			mp = (MultiPolygon) JTSGeomUtil.toMulti( mp.union(aFaceGeom) );
		}
		getObject().setGeom(mp);
	}

	public int getNumberOfNonDeletedFaces() {
		int n=0;
		for(AFace aFace : aFaces) if(!aFace.isDeleted()) n++;
		return n;
	}

	public void absorbStraits() {
		if(straits == null || straits.size() == 0) return;
		Collection<Geometry> geoms = new HashSet<Geometry>();
		geoms.add(getObject().getGeom());
		for(Polygon strait : straits) geoms.add(strait);
		getObject().setGeom(Union.get(geoms));
		straits = null;
	}

}
