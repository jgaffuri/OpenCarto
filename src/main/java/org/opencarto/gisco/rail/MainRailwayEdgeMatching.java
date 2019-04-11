/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.HashMap;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.edgematching.NetworkEdgeMatching;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayEdgeMatching {

	//find way to exclude lines really far away (PT)
	//TODO: remove sections not in their countries, far and not connected (?)
	//TODO: when section is similar to another one (min dist + hausdorf small), remove the one which is not from the country.

	//TODO: get OSM, for comparision
	//TODO: improve input file: projection 3035, features with null geometries for IT and RO. Multi geoms for IT. overlapping features for PT. fix DK. get attributes. get more countries. get better resolution (PT, IE, DE, PL).


	public static void main(String[] args) throws Exception {
		String cntAtt = "CNTR";

		//resolution data
		HashMap<String,Double> resolutions = new HashMap<String,Double>();
		resolutions.put("BE", 1.0);
		resolutions.put("LU", 1.5);
		resolutions.put("AT", 1.5);
		resolutions.put("NL", 5.0);
		resolutions.put("NO", 5.0);
		resolutions.put("CH", 6.0);
		resolutions.put("SE", 6.0);
		resolutions.put("FR", 7.9);
		resolutions.put("ES", 8.0);
		resolutions.put("FI", 8.0);
		resolutions.put("UK", 9.0);
		resolutions.put("IT", 14.0);
		resolutions.put("PL", 25.0);
		resolutions.put("DE", 50.0);
		resolutions.put("IE", 70.0);
		resolutions.put("PT", 500.0);

		//TODO check these values
		resolutions.put("RO", 500.0);
		resolutions.put("EL", 500.0);
		resolutions.put("DK", 500.0);
		resolutions.put("EE", 500.0);

		/*ArrayList<String> cnts = new ArrayList<String>();
		cnts.addAll( resolutions.keySet() );
		cnts.sort(new Comparator<String>() {
			@Override
			public int compare(String cnt1, String ctn2) { return (int)(10000000 * (resolutions.get(ctn2) - resolutions.get(cnt1))); }
		});*/



		System.out.println("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		//Filter fil = CQL.toFilter( "CNTR <> 'RO' AND CNTR <> 'EL' AND CNTR <> 'DK' AND CNTR <> 'EE'" );
		ArrayList<Feature> secs = SHPUtil.loadSHP(basePath+"in/RailwayLinkClean.shp").fs;
		System.out.println(secs.size());

		System.out.println("Ensure input geometries are simple");
		FeatureUtil.ensureGeometryNotAGeometryCollection(secs);

		System.out.println("Initialise EM tag");
		for(Feature s : secs) s.getProperties().put("EM", "");

		System.out.println("Clip with buffer difference of all sections, depending on country resolution");
		secs = NetworkEdgeMatching.clip(secs, resolutions, cntAtt);

		System.out.println("Build matching edges");
		ArrayList<Edge> mes = NetworkEdgeMatching.getMatchingEdges(secs, resolutions, 1.5, cntAtt);

		System.out.println("Save matching edges " + mes.size());
		SHPUtil.saveSHP(Edge.getEdgeFeatures(mes), basePath+"out/EM/matching_edges.shp", SHPUtil.getCRS(basePath+"in/RailwayLink.shp"));

		System.out.println("Extend sections with matching edges");
		NetworkEdgeMatching.extendSectionswithMatchingEdges(mes, resolutions, cntAtt);

		System.out.println("Save output " + secs.size());
		SHPUtil.saveSHP(secs, basePath+"out/EM/RailwayLinkEM.shp", SHPUtil.getCRS(basePath+"in/RailwayLink.shp"));

		System.out.println("End");
	}

}
