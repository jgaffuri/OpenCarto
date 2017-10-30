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
		pIni.runRecursively();
	}

	public class Partition{
		Envelope env;
		Polygon extend = null;
		Collection<Feature> features = null;
		Collection<Partition> subPartitions;

		Partition(double xMin, double xMax, double yMin, double yMax){ this(new Envelope(xMin,xMax,yMin,yMax)); }
		Partition(Envelope env){
			this.env = env;
			extend = JTS.toGeometry(this.env);
		}

		private void setFeatures(Collection<Feature> fs, boolean computeIntersections) {
			if(!computeIntersections) {
				features = fs;
				return;
			}

			features = new HashSet<Feature>();
			for(Feature f : fs) {
				Geometry g = f.getGeom();
				Envelope env_ = g.getEnvelopeInternal();
				if(!env.intersects(env_)) continue;

				//feature fully in the envelope
				if(env.contains(env_)) {
					features.add(f);
					continue;
				}

				//check if feature intersects envelope
				Geometry inter = g.intersection(extend);
				if(inter.isEmpty()) continue;
				if(inter.getArea()==0) continue;

				//create intersection feature
				Feature f_ = new Feature();
				f_.setGeom(inter);
				f_.id = f.id;
				features.add(f_);
			}
		}

		//determine if a partition is to large
		private boolean isTooLarge(double maxCoordinatesNumber) {
			int coordinatesNumber = 0;
			for(Feature f : features) coordinatesNumber += f.getGeom().getNumPoints();
			return coordinatesNumber > maxCoordinatesNumber;
		}

		//run process on the partition, decomposing it recursively if it is too large.
		private void runRecursively() {
			if(! isTooLarge(maxCoordinatesNumber)) run();
			else {
				//decompose in sub-partitions
				decompose();
				//run process on sub-partitions
				for(Partition sp : subPartitions) sp.runRecursively();
				//recompose sub-partitions
				recompose();
			}
		}

		//decompose the partiotion into four partitions
		private void decompose() {
			//TODO create four sub-partitions
			//clean top partition to avoid duplication of objects
			features.clear(); features=null;
		}

		//recompose partition
		private void recompose() {
			//TODO recompose
			//clean sub partitions
			subPartitions.clear(); subPartitions = null;
		}

		//run process on the partition
		private void run() {
			//TODO run generalisation on features
		}

	}

}
