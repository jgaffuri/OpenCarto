/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

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
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

/**
 * A tesselation unit, which consists of one or several AFaces.
 * It is an agent representing a multipolygon statistical unit.
 * 
 * @author julien Gaffuri
 *
 */
public class AUnit extends Agent {
	private final static Logger LOGGER = Logger.getLogger(AUnit.class);

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
					union = Union.get(geoms);
				} catch (Exception e2) {
					LOGGER.warn("Union.get failed for unit "+getId()+". Trying another union method. Message: "+e1.getMessage());
					union = null;
				}
			}
		}

		if(union==null || union.isEmpty()){
			LOGGER.error("Null union found when updating geometry of unit "+getId()+". Nb polygons="+geoms.size());
		} else
			union = (MultiPolygon) JTSGeomUtil.toMulti(union);

		getObject().setGeom(union);

		/*MultiPolygon mp = new GeometryFactory().createMultiPolygon(new Polygon[]{});
		for(AFace aFace : aFaces) {
			try {
				if(aFace.isDeleted()) continue;
				Geometry aFaceGeom = aFace.getObject().getGeometry();
				Geometry union = mp.union(aFaceGeom);
				mp = (MultiPolygon) JTSGeomUtil.toMulti(union);
			} catch (Exception e) {
				LOGGER.error("Error when building unit's geometry for unit "+this.getId()+": "+e.getMessage());
			}
		}
		getObject().setGeom(mp);*/
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
		//getObject().setGeom(Union.get(geoms));
		Geometry union = CascadedPolygonUnion.union(geoms);
		getObject().setGeom(JTSGeomUtil.toMulti(union));
		straits = null;
	}

}
