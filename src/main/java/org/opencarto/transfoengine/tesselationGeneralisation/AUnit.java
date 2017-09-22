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
	}

	public Feature getObject() { return (Feature)super.getObject(); }

	//the patches composing the units
	public Collection<AFace> aFaces = null;

	//the narrow gaps
	public Collection<Polygon> narrowGaps = null;

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
					union = Union.get(geoms);
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

	//deprecated
	public void absorbGaps() { absorbGaps(this.narrowGaps, true, false); }
	public void absorbGaps(Collection<Polygon> gaps, boolean clearAfter, boolean ensureTesselation) {
		if(gaps == null || gaps.size() == 0) return;
		MultiPolygon union = null;
		try {
			Collection<Polygon> all = new ArrayList<Polygon>();
			all.addAll(gaps);
			all.addAll(JTSGeomUtil.getPolygonGeometries(getObject().getGeom()));
			union = (MultiPolygon) JTSGeomUtil.toMulti(CascadedPolygonUnion.union(all));
			if(clearAfter) gaps.clear();
		} catch (Exception e) {
			LOGGER.warn("Could not fill gaps with CascadedPolygonUnion for unit "+getId()+". Message: "+e.getLocalizedMessage());
			//TODO try other unioning operation?
			return;
		}

		if(ensureTesselation){
			//TODO ensure partition!
		}

		getObject().setGeom(JTSGeomUtil.toMulti(union));
	}

	/*
	public void fillNarrowGaps(double resolution, double sizeDel, int quad, boolean ensureTesselation) {
		MultiPolygon geom = MorphologicalAnalysis.fillNarrowGaps(getObject().getGeom(), resolution, sizeDel, quad);

		if(ensureTesselation){
			//TODO ensure partition!
			//get intersecting units
		}

		getObject().setGeom(geom);
	}*/

}
