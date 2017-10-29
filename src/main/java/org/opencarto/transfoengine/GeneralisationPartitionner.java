/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * An utility to run partitionned processes.
 * 
 * @author julien Gaffuri
 *
 */
public class GeneralisationPartitionner {

	public Object runRecurssively(Collection<Feature> features) {


		if (partitionningNeeded(features)) {
			//partiionning
			Collection<Collection<Feature>> partitions = partition(features);
			//launch job on parts
			Collection<Object> results = new HashSet<Object>();
			for(Collection<Feature> partition : partitions) results.add(run(partition));
			//return reconciliated results
			return reconciliate(results);
		} else {
			return run(features);
		}
	}

	private boolean partitionningNeeded(Collection<Feature> features) {
		//TODO
		return false;
	}

	private Collection<Collection<Feature>> partition(Collection<Feature> features) {
		//TODO
		return null;
	}

	public Object run(Collection<Feature> features) {
		//TODO
		return null;
	}

	private Object reconciliate(Collection<Object> results) {
		//TODO
		return null;
	}

	public class Partition{
		Envelope env;
		Polygon extend = null;
		Collection<Feature> features = null;

		Partition(double xMin, double xMax, double yMin, double yMax){
			env = new Envelope(xMin,xMax,yMin,yMax);
			extend = JTSGeomUtil.createPolygon(xMin,yMin, xMax,yMin, xMax,yMax, xMin,yMax, xMin,yMin);
		}

		public void setFeatures(Collection<Feature> fs) {
			features = new HashSet<Feature>();
			for(Feature f : fs) {
				Geometry g = f.getGeom();
				Envelope env_ = g.getEnvelopeInternal();
				if(!env.intersects(env_)) continue;

				if(env.contains(env_)) {
					features.add(f);
					continue;
				}

				Geometry inter = g.intersection(extend);
				if(inter.isEmpty()) continue;
				if(inter.getArea()==0) continue;

				Feature f_ = new Feature();
				f_.setGeom(inter);
				f_.id = f.id;
				features.add(f_);
			}
		}

		public boolean isTooLarge(double maxCoordinatesNumber) {
			int coordinatesNumber = 0;
			for(Feature f : features) coordinatesNumber += f.getGeom().getNumPoints();
			return coordinatesNumber > maxCoordinatesNumber;
		}

	}

}
