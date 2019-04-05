/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.geotools.filter.text.cql2.CQL;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opengis.filter.Filter;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayEdgeMatching {

	public static void main(String[] args) throws Exception {

		//resolution data
		HashMap<String,Double> resolutions = new HashMap<String,Double>();
		resolutions.put("BE", 2.0);
		resolutions.put("LU", 1.5);
		resolutions.put("NL", 7.0);

		System.out.println("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		Filter f = CQL.toFilter( "CNTR = 'BE' OR CNTR = 'LU' OR CNTR = 'NL'" );
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"in/RailwayLink.shp", f).fs;
		System.out.println(tracks.size());

		System.out.println("Build spatial index");
		Quadtree tracksInd = new Quadtree();
		for(Feature t : tracks) tracksInd.insert(t.getGeom().getEnvelopeInternal(), t);

		System.out.println("Get list of countries ordered by resolution");
		ArrayList<String> cnts = new ArrayList<String>();
		cnts.addAll( resolutions.keySet() );
		cnts.sort(new Comparator<String>() {
			@Override
			public int compare(String cnt1, String ctn2) { return (int)(10000000 * (resolutions.get(ctn2) - resolutions.get(cnt1))); }
		});

		System.out.println("Go through countries, starting with the one with the highest resolution");
		for(String cnt : cnts) {
			//get all tracks of the cnt
			ArrayList<Feature> tracksCnt = new ArrayList<Feature>();
			for(Feature t : tracks) if(t.getProperties().get("CNTR").equals(cnt)) tracksCnt.add(t);

			double res = resolutions.get(cnt);
			//System.out.println(cnt+" "+res);
			for(Feature t : tracksCnt) {
				//get all tracks that are 'nearby'


			}
		}


		System.out.println("End");
	}

}
