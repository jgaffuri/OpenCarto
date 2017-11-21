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


	//get envelope of some features
	private static Envelope getEnvelope(Collection<Feature> features) { return getEnvelope(features, 1); }
	private static Envelope getEnvelope(Collection<Feature> features, double enlargementFactor) {
		Envelope env = features.iterator().next().getGeom().getEnvelopeInternal();
		for(Feature f : features) env.expandToInclude(f.getGeom().getEnvelopeInternal());
		env.expandBy((1-enlargementFactor)*env.getWidth(), (1-enlargementFactor)*env.getHeight());
		return env;
	}

	public static Collection<Feature> runRecursively(Operation op, Collection<Feature> features, int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		//create initial partition
		Partition p = new Partition(op, features, "0");
		//launch process
		p.runRecursively(maxCoordinatesNumber, objMaxCoordinateNumber);
		//return result
		return p.getFeatures();
	}



	private Envelope env;
	public Collection<Feature> features = null;
	public Collection<Feature> getFeatures() { return features; }
	private String code;
	public String getCode() { return code; }

	public interface Operation { void run(Partition p); }
	private Operation operation;

	Partition(Operation op, Collection<Feature> features, String code){
		this(op, getEnvelope(features, 1.001), code);
		this.features = features;
	}
	Partition(Operation op, double xMin, double xMax, double yMin, double yMax, String code){ this(op, new Envelope(xMin,xMax,yMin,yMax), code); }
	Partition(Operation op, Envelope env, String code) {
		this.operation = op;
		this.env = env;
		this.code = code;
	}

	//determine if the partition is to large: if it has too many vertices, or if it contains an object with too many vertices
	private int coordinatesNumber = 0, maxOCN = 0;
	private boolean isTooLarge(int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		coordinatesNumber = 0;
		maxOCN = 0;
		for(Feature f : features) {
			int nb = f.getGeom().getNumPoints();
			coordinatesNumber += nb;
			maxOCN = Math.max(maxOCN, nb);
		}
		return coordinatesNumber > maxCoordinatesNumber || maxOCN > objMaxCoordinateNumber;
	}


	//run process on the partition, decomposing it recursively if it is too large.
	private void runRecursively(int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		if(! isTooLarge(maxCoordinatesNumber, objMaxCoordinateNumber)) {
			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   not too large: Run process...");
			operation.run(this);
		}
		else {
			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   too large: Decompose it...");
			Collection<Partition> subPartitions = decompose();

			//run process on sub-partitions
			for(Partition sp : subPartitions)
				sp.runRecursively(maxCoordinatesNumber, objMaxCoordinateNumber);

			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   Recomposing");
			recompose(subPartitions);
		}
	}

	//decompose the partition into four partitions
	private Collection<Partition> decompose() {
		//create four sub-partitions
		double xMid = 0.5*(env.getMinX() + env.getMaxX()), yMid = 0.5*(env.getMinY() + env.getMaxY());
		Partition
		p1 = new Partition(operation, env.getMinX(), xMid, env.getMinY(), yMid, this.code+"1"),
		p2 = new Partition(operation, xMid, env.getMaxX(), env.getMinY(), yMid, this.code+"2"),
		p3 = new Partition(operation, env.getMinX(), xMid, yMid, env.getMaxY(), this.code+"3"),
		p4 = new Partition(operation, xMid, env.getMaxX(), yMid, env.getMaxY(), this.code+"4")
		;

		//fill it
		p1.cutAndSetFeatures(features);
		p2.cutAndSetFeatures(features);
		p3.cutAndSetFeatures(features);
		p4.cutAndSetFeatures(features);

		Collection<Partition> subPartitions = new ArrayList<Partition>();
		if(p1.features.size()>0) subPartitions.add(p1);
		if(p2.features.size()>0) subPartitions.add(p2);
		if(p3.features.size()>0) subPartitions.add(p3);
		if(p4.features.size()>0) subPartitions.add(p4);

		//clean top partition to avoid heavy duplication of objects
		features.clear(); features=null;

		return subPartitions;
	}

	private void cutAndSetFeatures(Collection<Feature> inFeatures) {

		features = new HashSet<Feature>();
		Polygon extend = JTS.toGeometry(this.env);

		for(Feature f : inFeatures) {
			Geometry g = f.getGeom();
			Envelope env_ = g.getEnvelopeInternal();
			if(!this.env.intersects(env_)) continue;

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

		//set reduced envelope
		if(features.size()>0) this.env = getEnvelope(features);

		if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   Features: "+features.size()+" kept from "+inFeatures.size()+". "+(int)(100*features.size()/inFeatures.size()) + "%");
	}


	//recompose partition
	private void recompose(Collection<Partition> subPartitions) {
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
	}

	@Override
	public String toString() {
		return code+" - size="+coordinatesNumber+"|"+maxOCN+" - nbFeatures="+features.size();
	}

}
