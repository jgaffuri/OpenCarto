/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.opencarto.algo.base.Union;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Agent;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

/**
 * A tesselation unit, which consists of one or several AFaces.
 * It is an agent representing a multipolygon statistical unit.
 * 
 * @author julien Gaffuri
 *
 */
public class AUnit extends Agent {
	private final static Logger LOGGER = Logger.getLogger(AUnit.class.getName());

	private ATesselation aTess;
	public ATesselation getAtesselation(){ return aTess; }

	public AUnit(Feature f, ATesselation aTess) {
		super(f);
		this.aTess=aTess;
		this.setId(f.id);
	}

	//the points that are supposed to be inside the unit, and might be used for a constraint
	public Collection<Point> points = null;

	public Feature getObject() { return (Feature)super.getObject(); }

	//the patches composing the units
	public Collection<AFace> aFaces = null;

	//update unit geometry from face geometries
	public void updateGeomFromFaceGeoms(){
		if(aFaces == null) return;
		Collection<Geometry> geoms = new HashSet<Geometry>();
		for(AFace aFace : aFaces) {
			if(aFace.isDeleted()) continue;
			Geometry aFaceGeom = aFace.getObject().getGeometry();
			if(aFaceGeom==null || aFaceGeom.isEmpty()){
				LOGGER.error("Error when building unit's geometry for unit "+this.getId()+": Face as null/empty geometry "+aFace.getId());
				continue;
			}
			geoms.add(aFaceGeom);
		}

		Geometry union;
		try {
			union = CascadedPolygonUnion.union(geoms);
		} catch (Exception e) {
			LOGGER.warn("CascadedPolygonUnion failed for unit "+getId()+". Trying another union method. Message: "+e.getMessage());
			try {
				union = new GeometryFactory().buildGeometry(geoms).union();
			} catch (Exception e1) {
				LOGGER.warn("Collection<Geometry>.union failed for unit "+getId()+". Trying another union method. Message: "+e1.getMessage());
				try {
					union = Union.getPolygonUnion(geoms);
				} catch (Exception e2) {
					LOGGER.warn("Union.get failed for unit "+getId()+". Trying another union method. Message: "+e1.getMessage());
					union = null;
				}
			}
		}

		if(union==null || union.isEmpty()){
			LOGGER.warn("Null union found when updating geometry of unit "+getId()+". Nb polygons="+geoms.size());
		} else
			union = (MultiPolygon) JTSGeomUtil.toMulti(union);

		getObject().setGeom(union);
	}

	public int getNumberOfNonDeletedFaces() {
		int n=0;
		for(AFace aFace : aFaces) if(!aFace.isDeleted()) n++;
		return n;
	}

	public void clear() {
		if(aFaces != null) aFaces.clear(); aFaces = null;
		if(points != null) points.clear(); points = null;
	}




	void linkPointsToFaces() {
		if(points == null) return;
		for(Point pt : points) {
			AFace af = getAFace(pt);
			if(af==null) {
				LOGGER.warn("Could not find any face for point "+pt.getCoordinate()+" belonging to unit "+getId());
				continue;
			}
			if(af.points == null) af.points = new ArrayList<Point>();
			af.points.add(pt);
		}
	}

	private AFace getAFace(Point pt) {
		for(AFace af : aFaces) if(af.getObject().getGeometry().contains(pt)) return af;
		return null;
	}

}
