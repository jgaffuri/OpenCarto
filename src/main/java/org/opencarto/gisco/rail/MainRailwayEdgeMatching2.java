/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayEdgeMatching2 {

	public static void main(String[] args) throws Exception {
		boolean b;

		//resolution data
		HashMap<String,Double> resolutions = new HashMap<String,Double>();
		resolutions.put("BE", 0.8);
		resolutions.put("LU", 1.2);
		resolutions.put("AT", 1.3);
		resolutions.put("NL", 5.0);
		resolutions.put("NO", 5.1);
		resolutions.put("CH", 6.0);
		resolutions.put("SE", 6.1);
		resolutions.put("FR", 7.9);
		resolutions.put("ES", 8.0);
		resolutions.put("FI", 8.1);
		resolutions.put("UK", 8.4);
		resolutions.put("IT", 14.0);
		resolutions.put("PL", 25.0);
		resolutions.put("DE", 40.0);
		resolutions.put("IE", 70.0);
		resolutions.put("PT", 250.0);

		//TODO get real values
		resolutions.put("RO", 250.0);
		resolutions.put("EL", 250.0);
		resolutions.put("DK", 250.0);
		resolutions.put("EE", 250.0);


		System.out.println("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		ArrayList<Feature> secs = SHPUtil.loadSHP(basePath+"in/RailwayLinkClean.shp").fs;
		System.out.println(secs.size());

		System.out.println("Build spatial index");
		Quadtree si = new Quadtree();
		for(Feature c : secs) si.insert(c.getGeom().getEnvelopeInternal(), c);

		System.out.println("Get list of countries ordered by resolution");
		ArrayList<String> cnts = new ArrayList<String>();
		cnts.addAll( resolutions.keySet() );
		cnts.sort(new Comparator<String>() {
			@Override
			public int compare(String cnt1, String ctn2) { return (int)(10000000 * (resolutions.get(ctn2) - resolutions.get(cnt1))); }
		});
		System.out.println(cnts);

		System.out.println("Get worst resolution value");
		double resMax = Collections.max(resolutions.values());

		System.out.println("Initialise EM tag");
		for(Feature s : secs) s.getProperties().put("EM", "no");


		System.out.println("Build matching nodes");
		ArrayList<Feature> secsOut = new ArrayList<Feature>();
		for(Feature s : secs) {
			String cnt = s.getProperties().get("CNTR").toString();
			double res = resolutions.get(cnt);
			Geometry g = s.getGeom();
			Envelope env = g.getEnvelopeInternal(); env.expandBy(res*1.01);
			for(Object s2 : si.query(env)) {
				Feature s_ = (Feature) s2;

				//filter
				if(s == s_) continue;
				if(! s_.getGeom().getEnvelopeInternal().intersects(env)) continue;
				if(s_.getGeom().isEmpty()) continue;
				String cnt_ = s_.getProperties().get("CNTR").toString();
				if(cnt_.equals(cnt)) continue;
				double res_ = resolutions.get(cnt_);
				if(res_ > res) continue; //s to be cut by those with better resolution
				if(MainRailwayEdgeMatching.areConnected( (LineString)s.getGeom(), (LineString)s_.getGeom())) continue;

				//tag
				if(!s_.getProperties().get("EM").equals("changed")) s_.getProperties().put("EM", "involved");

				g = g.difference( s_.getGeom().buffer(res) );
				changed=true;
				if(g.isEmpty()) break;
			}

		}






		System.out.println("Save output " + secs.size());
		SHPUtil.saveSHP(secs, basePath+"out/EM/RailwayLinkEM.shp", SHPUtil.getCRS(basePath+"in/RailwayLink.shp"));

		System.out.println("End");
	}


}
