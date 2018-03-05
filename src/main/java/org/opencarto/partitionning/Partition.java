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
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

/**
 * @author julien Gaffuri
 *
 */
public class Partition {
	private final static Logger LOGGER = Logger.getLogger(Partition.class.getName());

	public static Collection<Feature> runRecursively(Collection<Feature> features, Operation op, int maxCoordinatesNumber, int objMaxCoordinateNumber, boolean ignoreRecomposition) {
		Partition p = new Partition("0", features, op);
		p.runRecursively(maxCoordinatesNumber, objMaxCoordinateNumber, ignoreRecomposition);
		return p.getFeatures();
	}



	private String code;
	public String getCode() { return code; }

	public Collection<Feature> features = null;
	public Collection<Feature> getFeatures() { return features; }

	public interface Operation { void run(Partition p); }
	private Operation operation;

	private Envelope env;
	public Envelope getEnvelope() { return env; }
	public Polygon getExtend() { return JTS.toGeometry(this.env); }

	private Partition(String code, Collection<Feature> features, Operation op){
		this(code, op, FeatureUtil.getEnvelope(features, 1.001));
		this.features = features;
	}
	private Partition(String code, Operation op, double xMin, double xMax, double yMin, double yMax){ this(code, op, new Envelope(xMin,xMax,yMin,yMax)); }
	private Partition(String code, Operation op, Envelope env) {
		this.code = code;
		this.operation = op;
		this.env = env;
	}

	//determine if the partition is to large: if it has too many vertices, or if it contains an object with too many vertices
	private int coordinatesNumber = 0, maxFCN = 0;
	private boolean isTooLarge(int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		coordinatesNumber = 0;
		maxFCN = 0;
		for(Feature f : features) {
			int fcn = f.getGeom().getNumPoints();
			coordinatesNumber += fcn;
			maxFCN = Math.max(maxFCN, fcn);
		}
		return coordinatesNumber > maxCoordinatesNumber || maxFCN > objMaxCoordinateNumber;
	}


	//run process on the partition, decomposing it recursively if it is too large.
	private void runRecursively(int maxCoordinatesNumber, int objMaxCoordinateNumber, boolean ignoreRecomposition) {
		if(! isTooLarge(maxCoordinatesNumber, objMaxCoordinateNumber)) {
			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   not too large: Run process...");
			operation.run(this);
		} else {
			if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   too large: Decompose it...");
			Collection<Partition> subPartitions = decompose();

			//run process on sub-partitions
			for(Partition sp : subPartitions)
				sp.runRecursively(maxCoordinatesNumber, objMaxCoordinateNumber, ignoreRecomposition);

			if(!ignoreRecomposition) {
				if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   Recomposing");
				recompose(subPartitions);
			}
		}
	}

	//decompose the partition into four partitions
	private Collection<Partition> decompose() {
		//create four sub-partitions
		//TODO extract random stuff as a parameter?
		double xMid = env.getMinX() + (0.5+(Math.random()-0.5)*0.02)*(env.getMaxX() - env.getMinX());
		double yMid = env.getMinY() + (0.5+(Math.random()-0.5)*0.02)*(env.getMaxY() - env.getMinY());

		Partition
		p1 = new Partition(this.code+"1", operation, env.getMinX(), xMid, yMid, env.getMaxY()),
		p2 = new Partition(this.code+"2", operation, xMid, env.getMaxX(), yMid, env.getMaxY()),
		p3 = new Partition(this.code+"3", operation, env.getMinX(), xMid, env.getMinY(), yMid),
		p4 = new Partition(this.code+"4", operation, xMid, env.getMaxX(), env.getMinY(), yMid)
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
		Polygon extend = getExtend();

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
			inter = JTSGeomUtil.keepOnlyPolygonal(inter);
			f_.setGeom(inter);
			f_.getProperties().putAll(f.getProperties());
			f_.id = f.id;
			f_.setProjCode(f.getProjCode());
			features.add(f_);
		}

		//set reduced envelope
		if(features.size()>0) this.env = FeatureUtil.getEnvelope(features);

		if(LOGGER.isTraceEnabled()) LOGGER.trace(this.code+"   Features: "+features.size()+" kept from "+inFeatures.size()+". "+(int)(100*features.size()/inFeatures.size()) + "%");
	}


	//recompose partition
	private void recompose(Collection<Partition> subPartitions) {

		//gather pieces together
		HashMap<String,Collection<Geometry>> index = new HashMap<String,Collection<Geometry>>();
		for(Partition p : subPartitions)
			for(Feature f : p.features) {
				Collection<Geometry> col = index.get(f.id);
				if(col == null) {
					col = new ArrayList<Geometry>();
					index.put(f.id, col);
				}
				col.add(f.getGeom());
			}

		//get features with pieces together
		features = new HashSet<Feature>();
		HashSet<String> fIds = new HashSet<String>();
		for(Partition p : subPartitions)
			for(Feature f : p.features) {
				if(fIds.contains(f.id)) continue;
				fIds.add(f.id);
				features.add(f);
				Collection<Geometry> pieces = index.get(f.id);
				if(pieces.size()==1)
					f.setGeom(pieces.iterator().next());
				else {
					Geometry union = CascadedPolygonUnion.union(pieces);
					f.setGeom(union);
				}
			}
		index.clear();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		//print basic information on partition size
		sb
		.append("Partition ").append(code).append(" -")
		.append(" CoordNb=").append(coordinatesNumber)
		.append(" MaxFCN=").append(maxFCN)		
		.append(" FeatNb=").append(features.size())
		;

		//if number of features is low, show their ids
		if(features.size() <=5) {
			sb.append(" id="); int i=0;
			for(Feature f : features) {
				i++;
				sb.append(f.id).append(",");
				if(i>=4) break;
			}
		}

		return sb.toString();
	}



	//build a dataset of partition areas, with some information on each partition
	public static Collection<Feature> getPartitionDataset(Collection<Feature> features, int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		final Collection<Feature> fs = new ArrayList<Feature>();
		final int projCode = features.iterator().next().getProjCode();

		Partition.runRecursively(features, new Operation() {
			public void run(Partition p) {
				LOGGER.info(p.toString());
				double area = p.env.getArea();
				Feature f = new Feature();
				f.setProjCode(projCode);
				f.setGeom(p.getExtend());
				f.getProperties().put("code", p.code);
				f.getProperties().put("f_nb", p.features.size());
				f.getProperties().put("c_nb", p.coordinatesNumber);
				f.getProperties().put("c_dens", p.coordinatesNumber/area);
				f.getProperties().put("maxfcn", p.maxFCN);
				f.getProperties().put("area", area);
				fs.add(f);
			}}, maxCoordinatesNumber, objMaxCoordinateNumber, true);

		return fs;
	}



	/*
	public static void main(String[] args) {
		//LOGGER.setLevel(Level.ALL);
		System.out.println("Load");
		ArrayList<Feature> features = SHPUtil.loadSHP("/home/juju/Bureau/nuts_gene_data/commplus/COMM_PLUS_clean1.shp", 4258).fs;
		System.out.println("Compute");
		Collection<Feature> fs = getPartitionDataset(features, 3000000, 15000);
		System.out.println("Save");
		SHPUtil.saveSHP(fs, "/home/juju/Bureau/", "partition.shp");
	}
	 */

}
