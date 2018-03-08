/**
 * 
 */
package org.opencarto.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author julien Gaffuri
 *
 */
public class FeatureUtil {
	private final static Logger LOGGER = Logger.getLogger(FeatureUtil.class.getName());


	public static STRtree getSTRtree(Collection<Feature> fs) {
		STRtree index = new STRtree();
		for(Feature f : fs) index.insert(f.getGeom().getEnvelopeInternal(), f);
		return index;
	}
	public static Quadtree getQuadtree(Collection<Feature> fs) {
		Quadtree index = new Quadtree();
		for(Feature f : fs) index.insert(f.getGeom().getEnvelopeInternal(), f);
		return index;
	}

	public static STRtree getSTRtreeCoordinates(Collection<Feature> fs) {
		STRtree index = new STRtree();
		for(Feature f : fs) {
			for(Coordinate c : f.getGeom().getCoordinates())
				//TODO ensure no coordinate at same location
				index.insert(new Envelope(c), c);
		}
		return index;
	}

	public static int getNumberVertices(Collection<Feature> fs) {
		int nb = 0;
		for(Feature f : fs) nb += f.getGeom().getNumPoints();
		return nb;
	}


	//get envelope of features
	public static Envelope getEnvelope(Collection<Feature> features) { return getEnvelope(features, 1); }
	public static Envelope getEnvelope(Collection<Feature> features, double enlargementFactor) {
		if(features.size() == 0) {
			LOGGER.warn("No features in partition - cannot compute envelope");
			return null;
		}
		Envelope env = features.iterator().next().getGeom().getEnvelopeInternal();
		for(Feature f : features) env.expandToInclude(f.getGeom().getEnvelopeInternal());
		env.expandBy((enlargementFactor-1)*env.getWidth(), (enlargementFactor-1)*env.getHeight());
		return env;
	}

	//check if an attribute is an identifier (that is it is unique)
	public static HashMap<String,Integer> checkIdentfier(Collection<Feature> fs, String idAtt) {
		//build id count index
		HashMap<String,Integer> index = new HashMap<String,Integer>();
		for(Feature f : fs) {
			Object id_ = f.getProperties().get(idAtt);
			if(id_ == null) {
				LOGGER.warn("Could not find attribute " + idAtt + " for feature " + f.id);
				continue;
			}
			String id = id_.toString();
			Integer count = index.get(id);
			if(count == null) index.put(id, 1); else index.put(id, count+1);
		}
		//keep only the elements with more that one count
		HashMap<String,Integer> out = new HashMap<String,Integer>();
		for(Entry<String,Integer> e : index.entrySet())
			if(e.getValue() > 1) out.put(e.getKey(), e.getValue());
		return out;
	}

	//check if an attribute is an identifier (that is it is unique)
	public static int getVerticesNumber(Collection<Feature> fs) {
		int nb=0;
		for(Feature f : fs) {
			if(f.getGeom() == null) {
				LOGGER.warn("Could not count the number of vertices of feature "+f.id+": Null geometry.");
				continue;
			}
			nb += f.getGeom().getNumPoints();
		}
		return nb;
	}

	//considering multi/polygonal features, get the patches that are smallest than an area threshold
	public static ArrayList<Map<String, Object>> analysePolygonsSizes(Collection<Feature> fs, double areaThreshold) {
		ArrayList<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
		for(Feature f : fs) {
			Collection<Geometry> polys = JTSGeomUtil.getGeometries( JTSGeomUtil.keepOnlyPolygonal(f.getGeom()) );
			for(Geometry poly : polys) {
				double area = poly.getArea();
				if( area > areaThreshold ) continue;
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", f.id);
				m.put("area", area);
				m.put("position", poly.getCentroid().getCoordinate());
				out.add(m);
			}
		}
		return out;
	}

}
