/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.Collection;
import java.util.HashSet;

import org.geotools.geometry.jts.JTS;
import org.opencarto.datamodel.Feature;

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
	int maxCoordinatesNumber = 100000;

	public void runRecursively(Collection<Feature> features) {
		//get envelope of input features
		Envelope env = features.iterator().next().getGeom().getEnvelopeInternal();
		for(Feature f : features) env.expandToInclude(f.getGeom().getEnvelopeInternal());

		//create initial partition
		Partition pIni = new Partition(env);
		pIni.setFeatures(features, false);

		//launch process
		runRecursively(pIni);
	}

	public void runRecursively(Partition p) {
		if(! p.isTooLarge(maxCoordinatesNumber)) run(p);
		else {
			//decompose in sub-partitions
			Collection<Partition> sps = p.getSubPartitions();
			//run process on sub-partitions
			for(Partition sp : sps) runRecursively(sp);
			//recompose result
			//TODO
		}
	}


	public Object run(Partition p) {
		//TODO launch generalisation here
		return null;
	}

	private Object reconciliate(Collection<Partition> ps) {
		//TODO
		return null;
	}

	public class Partition{
		Envelope env;
		Polygon extend = null;
		Collection<Feature> features = null;

		Partition(double xMin, double xMax, double yMin, double yMax){ this(new Envelope(xMin,xMax,yMin,yMax)); }
		Partition(Envelope env){
			this.env = env;
			extend = JTS.toGeometry(this.env);
		}

		public void setFeatures(Collection<Feature> fs, boolean computeIntersections) {
			if(!computeIntersections) {
				features = fs;
				return;
			}
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

		public Collection<Partition> getSubPartitions() {
			//TODO create four sub-partitions and return them
			return null;
		}

	}

}
