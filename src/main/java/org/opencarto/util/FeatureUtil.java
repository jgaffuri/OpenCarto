/**
 * 
 */
package org.opencarto.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;
import org.opencarto.datamodel.Feature;

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

	public static Coordinate getMedianPosition(Collection<Feature> fs) {
		Coordinate c = new Coordinate();
		{
			ArrayList<Double> s = new ArrayList<Double>(); double[] s_;
			for(Feature f : fs) for(Coordinate c_ : f.getGeom().getCoordinates()) s.add(c_.x);
			s_ = ArrayUtils.toPrimitive(s.toArray(new Double[s.size()]));
			c.x = StatUtils.percentile(s_ ,50);
			s_ = null;
			s.clear();
		}{
			ArrayList<Double> s = new ArrayList<Double>(); double[] s_;
			for(Feature f : fs) for(Coordinate c_ : f.getGeom().getCoordinates()) s.add(c_.y);
			s_ = ArrayUtils.toPrimitive(s.toArray(new Double[s.size()]));
			c.y = StatUtils.percentile(s_ ,50);
			s_ = null;
			s_ = null;
			s.clear();
		}
		return c;
	}

	//check if an attribute is an identifier (that is it is unique)
	public static HashMap<String,Integer> checkIdentfier(Collection<Feature> fs, String idAtt) {
		//build id count index
		HashMap<String,Integer> index = new HashMap<String,Integer>();
		for(Feature f : fs) {
			Object id_ = f.get(idAtt);
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
	public static ArrayList<Map<String, Object>> getInfoSmallPolygons(Collection<Feature> fs, double areaThreshold) {
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

	public static Collection<Geometry> getGeometries(ArrayList<Feature> fs) {
		Collection<Geometry> gs = new ArrayList<Geometry>();
		for(Feature f : fs) gs.add(f.getGeom());
		return gs ;
	}
	public static Collection<MultiLineString> getGeometriesMLS(ArrayList<Feature> fs) {
		Collection<MultiLineString> gs = new ArrayList<MultiLineString>();
		for(Feature f : fs) gs.add((MultiLineString) f.getGeom());
		return gs ;
	}
	public static Collection<LineString> getGeometriesLS(ArrayList<Feature> fs) {
		Collection<LineString> gs = new ArrayList<LineString>();
		for(Feature f : fs) gs.add((LineString) f.getGeom());
		return gs ;
	}




	public static void dissolveById(Collection<Feature> fs) {
		//index features by id
		HashMap<String,List<Feature>> ind = new HashMap<String,List<Feature>>();
		for(Feature f : fs) {
			List<Feature> col = ind.get(f.id);
			if(col == null) {
				col = new ArrayList<Feature>();
				ind.put(f.id, col);
			}
			col.add(f);
		}

		//merge features having same id
		for(List<Feature> col : ind.values()) {
			if(col.size() == 1) continue;
			Collection<MultiPolygon> polys = new ArrayList<MultiPolygon>();
			for(Feature f : col) polys.add((MultiPolygon) f.getGeom());
			MultiPolygon mp = (MultiPolygon) JTSGeomUtil.toMulti(CascadedPolygonUnion.union(polys));
			for(int i=1; i<col.size(); i++) fs.remove(col.get(i));
			col.get(0).setGeom(mp);
		}
	}

	public static Collection<Feature> dissolve(Collection<Feature> fs, String propName) {
		//index features by property
		HashMap<String,List<Feature>> ind = new HashMap<String,List<Feature>>();
		for(Feature f : fs) {
			String prop = (String) f.get(propName);
			List<Feature> col = ind.get(prop);
			if(col == null) {
				col = new ArrayList<Feature>();
				ind.put(prop, col);
			}
			col.add(f);
		}

		//merge features having same property
		Collection<Feature> out = new ArrayList<Feature>();
		for(Entry<String,List<Feature>> e : ind.entrySet()) {
			Feature f = new Feature();
			f.set(propName, e.getKey());
			Collection<MultiPolygon> polys = new ArrayList<MultiPolygon>();
			for(Feature f_ : e.getValue()) polys.add((MultiPolygon) f_.getGeom());
			MultiPolygon mp = (MultiPolygon) JTSGeomUtil.toMulti(CascadedPolygonUnion.union(polys));
			f.setGeom(mp);
			out.add(f);
		}
		return out;
	}


	public static Collection<Feature> toFeatures(Collection<Map<String, Object>> ps) {
		Collection<Feature> out = new ArrayList<Feature>();
		for(Map<String, Object> p : ps) {
			Feature f = new Feature();
			f.getProperties().putAll(p);
			out.add(f);
		}
		return out;
	}
	public static Collection<Feature> toFeatures(ArrayList<HashMap<String, String>> ps) {
		Collection<Feature> out = new ArrayList<Feature>();
		for(Map<String, String> p : ps) {
			Feature f = new Feature();
			f.getProperties().putAll(p);
			out.add(f);
		}
		return out;
	}


	//get all property values
	public static Set<String> getPropValues(Collection<Feature> fs, String propKey) {
		Set<String> out = new HashSet<String>();
		for(Feature f : fs) out.add(f.get(propKey).toString());
		return out;
	}
	//get all property values
	public static List<String> getPropValuesAsList(Collection<Feature> fs, String propKey) {
		List<String> out = new ArrayList<String>();
		out.addAll(getPropValues(fs, propKey));
		return out;
	}

	public static HashMap<String, Feature> index(Collection<Feature> fs, String indexKey) {
		HashMap<String, Feature> out = new HashMap<String, Feature>();
		for(Feature f : fs) out.put(f.get(indexKey).toString(), f);
		return out;
	}

	//get set of attribute keys of several features
	public static Set<String> getAttributesSet(Feature... fs) {
		Set<String> keys = new HashSet<>();
		for(Feature f : fs)
			keys.addAll(f.getProperties().keySet());
		return keys;
	}


	//keep only features with non empty geometry
	public static Collection<Feature> filterFeaturesWithNonEmptyGeometries(Collection<Feature> fs) {
		HashSet<Feature> out = new HashSet<Feature>();
		for(Feature f : fs) if(!f.getGeom().isEmpty()) out.add(f);
		return out;
	}

}
