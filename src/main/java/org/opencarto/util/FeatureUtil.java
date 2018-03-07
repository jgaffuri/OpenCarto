/**
 * 
 */
package org.opencarto.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
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
	public static HashMap<String,Integer> getNumberVertices(Collection<Feature> fs, String idC) {
		HashMap<String,Integer> index = new HashMap<String,Integer>();
		for(Feature f : fs) {
			String id = f.getProperties().get(idC).toString();
			Integer cnt = index.get(id);
			if(cnt == null) index.put(id, 1); else index.put(id, cnt+1);
		}
		HashMap<String,Integer> out = new HashMap<String,Integer>();
		for(Entry<String,Integer> e : index.entrySet())
			if(e.getValue() > 1) out.put(e.getKey(), e.getValue());
		return out;
	}

}
