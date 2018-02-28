/**
 * 
 */
package org.opencarto.util;

import java.util.Collection;

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

}
