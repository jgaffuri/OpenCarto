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
	int maxCoordinatesNumber = 100000;

	public void runRecurssively(Collection<Feature> features) {

		Partition pIni = null;
		//TODO get envelope of input features
		//TODO create initial partition (fast)
		runRecurssively(pIni);

	}

	public void runRecurssively(Partition p) {
		if(! p.isTooLarge(maxCoordinatesNumber)) run(p);
		else {
			//decompose and run on sub-partitions
			Collection<Partition> sps = p.getSubPartitions();
			for(Partition sp : sps) runRecurssively(sp);
			//TODO recompose
			
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

		public Collection<Partition> getSubPartitions() {
			//TODO create four sub-partitions and return them
			return null;
		}
		
	}

}
