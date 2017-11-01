/**
 * 
 */
package org.opencarto.partitionning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTS;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class Partition {
	private final static Logger LOGGER = Logger.getLogger(Partition.class);

	public static Collection<Feature> runRecursively(Operation op, Collection<Feature> features, int maxCoordinatesNumber) {
		//get envelope of input features
		Envelope env = features.iterator().next().getGeom().getEnvelopeInternal();
		for(Feature f : features) env.expandToInclude(f.getGeom().getEnvelopeInternal());
		if(LOGGER.isTraceEnabled()) LOGGER.trace("Initial envelope: "+env);

		//create initial partition
		Partition p = new Partition(op, env,"0");
		p.setFeatures(features, false);

		//launch process
		p.runRecursively(maxCoordinatesNumber);

		return p.getFeatures();
	}



	private Envelope env;
	public Collection<Feature> features = null;
	public Collection<Feature> getFeatures() { return features; }
	private Collection<Partition> subPartitions;
	private String code;
	public String getCode() { return code; }
	private int coordinatesNumber = 0;

	public interface Operation { void run(Partition p); }
	private Operation operation;

	Partition(Operation op, double xMin, double xMax, double yMin, double yMax, String code){ this(op, new Envelope(xMin,xMax,yMin,yMax), code); }
	Partition(Operation op, Envelope env, String code){
		this.env = env;
		this.code = code;
		this.operation = op;
	}

	public Polygon getExtend() {
		return JTS.toGeometry(this.env);
	}

	private void setFeatures(Collection<Feature> inFeatures, boolean computeIntersections) {
		if(!computeIntersections) {
			features = inFeatures;
			return;
		}

		features = new HashSet<Feature>();
		Polygon extend = getExtend();

		for(Feature f : inFeatures) {
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
			inter = JTSGeomUtil.toMulti(inter);
			f_.setGeom(inter);
			f_.getProperties().putAll(f.getProperties());
			f_.id = f.id;
			f_.setProjCode(f.getProjCode());
			features.add(f_);
		}

		if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   Features: "+features.size()+" kept from "+inFeatures.size()+". "+(int)(100*features.size()/inFeatures.size()) + "%");
	}

	//determine if a partition is to large
	private void computeCoordinatesNumber() {
		coordinatesNumber = 0;
		for(Feature f : features) coordinatesNumber += f.getGeom().getNumPoints();
	}
	private boolean isTooLarge(double maxCoordinatesNumber) {
		computeCoordinatesNumber();
		return coordinatesNumber > maxCoordinatesNumber;
	}

	//run process on the partition, decomposing it recursively if it is too large.
	private void runRecursively(int maxCoordinatesNumber) {
		if(! isTooLarge(maxCoordinatesNumber)) {
			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   not too large: Run process...");
			operation.run(this);
		}
		else {
			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   too large: Decompose it...");
			decompose();

			//run process on sub-partitions
			for(Partition sp : subPartitions) sp.runRecursively(maxCoordinatesNumber);

			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   Recomposing");
			recompose();
		}
	}

	//decompose the partition into four partitions
	private void decompose() {
		//create four sub-partitions
		double xMid = 0.5*(env.getMinX() + env.getMaxX()), yMid = 0.5*(env.getMinY() + env.getMaxY());
		Partition
		p1 = new Partition(operation, env.getMinX(), xMid, env.getMinY(), yMid, this.code+"1"),
		p2 = new Partition(operation, xMid, env.getMaxX(), env.getMinY(), yMid, this.code+"2"),
		p3 = new Partition(operation, env.getMinX(), xMid, yMid, env.getMaxY(), this.code+"3"),
		p4 = new Partition(operation, xMid, env.getMaxX(), yMid, env.getMaxY(), this.code+"4")
		;

		//fill it
		p1.setFeatures(features, true);
		p2.setFeatures(features, true);
		p3.setFeatures(features, true);
		p4.setFeatures(features, true);

		subPartitions = new ArrayList<Partition>();
		if(p1.features.size()>0) subPartitions.add(p1);
		if(p2.features.size()>0) subPartitions.add(p2);
		if(p3.features.size()>0) subPartitions.add(p3);
		if(p4.features.size()>0) subPartitions.add(p4);

		//clean top partition to avoid heavy duplication of objects
		features.clear(); features=null;
	}

	//recompose partition
	private void recompose() {
		//recompose
		HashMap<String,Feature> index = new HashMap<String,Feature>();
		for(Partition p : subPartitions)
			for(Feature f : p.features) {
				Feature f_ = index.get(f.id);
				if(f_ == null)
					index.put(f.id, f);
				else
					f_.setGeom( f_.getGeom().union(f.getGeom()) );
			}

		//extract features
		features = new HashSet<Feature>();
		features.addAll(index.values());
		index.clear();

		//clean sub partitions
		subPartitions.clear(); subPartitions = null;
	}

	@Override
	public String toString() {
		return code+" - size="+coordinatesNumber+" - nbFeatures="+features.size();
	}

}
