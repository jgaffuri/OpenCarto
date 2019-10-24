/**
 * 
 */
package org.opencarto.processes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.opencarto.ZoomExtend;
import org.opencarto.gps.datamodel.GPSTrace;
import org.opencarto.gps.datamodel.Lap;

import eu.europa.ec.eurostat.jgiscotools.algo.clustering.Aggregation;
import eu.europa.ec.eurostat.jgiscotools.algo.clustering.Clustering;
import eu.europa.ec.eurostat.jgiscotools.algo.clustering.ClusteringIndex;
import eu.europa.ec.eurostat.jgiscotools.algo.distances.Distance;
import eu.europa.ec.eurostat.jgiscotools.algo.distances.HausdorffDistance;
import eu.europa.ec.eurostat.jgiscotools.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class GPSGeneralisation extends GeneralisationProcess<GPSTrace> {

	public void perform(ArrayList<GPSTrace> fs, ZoomExtend zs){
		for(int z=zs.max; z>=zs.min; z--){
			//get resolution value
			double res = getResolution(z);
			System.out.println("Generalisation: "+z+" (resolution "+Util.round(res, 1)+"m)");

			//get zoom code to base on
			String zBase = z==zs.max? "" : String.valueOf(z+1);

			System.out.println("   Individual generalisation...");
			for(GPSTrace f: fs){
				Geometry geom=f.getGeom(zBase);
				if(geom==null) continue;
				f.setGeom(pre(geom, res), z);
			}

			System.out.println("   Clustering...");
			Clustering<GPSTrace> c = new Clustering<GPSTrace>();
			c.perform(fs, new GPSDistance(z,res), res*2, new GPSAggregation(z,res), false, new GPSClusteringIndex(z, fs));

			int nnNb=0;
			for(GPSTrace f: fs) if(f.getGeom(z)!=null) nnNb++;
			System.out.println("   " + fs.size() + " objects, " + nnNb + " non null");
		}
	}

	class GPSDistance implements Distance<GPSTrace>{
		private int z; private double res;
		public GPSDistance(int z, double res) { this.z = z; this.res = res; }

		public double get(GPSTrace t1, GPSTrace t2) {
			Geometry g1 = t1.getGeom(z);
			Geometry g2 = t2.getGeom(z);

			if(g1==null || g2==null)
				return Double.MAX_VALUE;

			if (g1.getEnvelopeInternal().distance(g2.getEnvelopeInternal()) > 2*res)
				return Double.MAX_VALUE;

			//if both are points, return normal distance
			if(g1 instanceof Point && g2 instanceof Point )
				return g1.distance(g2);

			//if point and line, return infiny
			if(g1 instanceof Point && g2 instanceof LineString )
				return Double.MAX_VALUE;
			if(g2 instanceof Point && g1 instanceof LineString )
				return Double.MAX_VALUE;

			//if both are lines, return hausdorf distance
			if(g1 instanceof LineString && g2 instanceof LineString )
				return new HausdorffDistance((LineString)g1, (LineString)g2).getDistance();

			//if both are surfaces, return normal distance
			if(g1.getDimension()==2 && g2.getDimension()==2 )
				return g1.distance(g2);

			//if mix line/surface, return difference length
			if(g1 instanceof LineString && g2.getDimension()==2)
				return g1.difference(g2).getLength();
			if(g2 instanceof LineString && g1.getDimension()==2)
				return g2.difference(g1).getLength();

			//if mix point/surface, return normal distance
			if(g1 instanceof Point && g2.getDimension()==2)
				return g1.distance(g2);
			if(g2 instanceof Point && g1.getDimension()==2)
				return g1.distance(g2);

			System.err.println("GPS distance not supported between: ");
			System.err.println(g1.getClass().getSimpleName());
			System.err.println(g2.getClass().getSimpleName());
			return Double.MAX_VALUE;
		}

	}

	class GPSAggregation implements Aggregation<GPSTrace>{
		private boolean debug = true;
		private int z; private double res;
		public GPSAggregation(int z, double res) { this.z = z; this.res = res; }

		public void aggregate(Collection<GPSTrace> objs, GPSTrace t1, GPSTrace t2) {
			Geometry g1 = t1.getGeom(z);
			Geometry g2 = t2.getGeom(z);

			if(debug) System.out.print("Clustering: " + g1.getClass().getSimpleName() + "(" +t1.getComponents().size()+ ") & "+ g2.getClass().getSimpleName()+"("+t2.getComponents().size()+ ")");

			//remove
			t1.setGeom(null, z);
			t2.setGeom(null, z);
			if(!index.remove(g1.getEnvelopeInternal(), t1)) System.err.println("Error in spatial index update!");
			if(!index.remove(g2.getEnvelopeInternal(), t2)) System.err.println("Error in spatial index update!");


			//build aggregate geometry
			Geometry geomAgg = null;
			if(g1 instanceof LineString && g2 instanceof LineString){
				geomAgg = g1;
			}
			else{
				if(g1.getDimension()<2) g1 = g1.buffer(res);
				if(g2.getDimension()<2) g2 = g2.buffer(res);
				geomAgg = g1.union(g2);
				geomAgg = geomAgg.buffer(3*res);
				geomAgg = geomAgg.buffer(-3*res);
			}
			geomAgg = pre(geomAgg, res);
			if(debug) System.out.print(" ->  " + geomAgg.getClass().getSimpleName());

			//create aggregated trace
			GPSTrace aggTrace = new GPSTrace(new ArrayList<Lap>());
			aggTrace.setGeom(geomAgg, z);
			if(t1.getComponents().size()==0) aggTrace.getComponents().add(t1);
			else aggTrace.getComponents().addAll(t1.getComponents());
			if(t2.getComponents().size()==0) aggTrace.getComponents().add(t2);
			else aggTrace.getComponents().addAll(t2.getComponents());

			if(debug) System.out.println("("+aggTrace.getComponents().size()+ ")");
			objs.add(aggTrace);
			index.insert(geomAgg.getEnvelopeInternal(), aggTrace);
		}
	}

	private Quadtree index;
	class GPSClusteringIndex implements ClusteringIndex<GPSTrace>{
		private int z;
		public GPSClusteringIndex(int z, ArrayList<GPSTrace> fs){
			this.z = z;
			//build spatial index
			index = new Quadtree();
			for(GPSTrace t:fs){
				Geometry g = t.getGeom(z);
				if(g==null) continue;
				index.insert(g.getEnvelopeInternal(), t);
			}
		}

		public List<GPSTrace> getCandidates(GPSTrace obj, double distance) {
			if(obj.getGeom(z)==null) return new ArrayList<GPSTrace>();
			Envelope env = obj.getGeom(z).getEnvelopeInternal();
			env.expandBy(distance);
			return index.query(env);
		}

	}

}
