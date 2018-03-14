/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.GraphSHPUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

/**
 * 
 * Some tests on ORM generalisation
 * 
 * @author julien Gaffuri
 *
 */
public class MainORMGene {
	public final static Logger LOGGER = Logger.getLogger(MainORMGene.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		//focus on sweden. Focus first on tracks
		//https://www.openrailwaymap.org/
		//https://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging

		//is it possible to filter OSM based on tagging to obtain something comparable to ERM? -> Yes.
		//filtering: ambiguity with "abandonned". Choice: remove it. Tramway/subway. Choice: remove it and focus on train.
		/*
"railway"!='construction'
AND "railway"!='dismantled'
AND "railway"!='elevator'
AND "railway"!='funicular'
AND "railway"!='historic'
AND "railway"!='historic_path'
AND "railway"!='historical'
AND "railway" != 'miniature'
AND "railway"!='planned'
AND "railway" != 'platform'
AND "railway" != 'platform_edge'
AND "railway" != 'proposed'
AND "railway" != 'razed'
AND "railway" != 'turntable'
AND "railway" != 'abandoned'
AND "railway" != 'tram'
AND "railway" != 'subway'
		 */
		//Conclusion: filtering analysis based on tags. Accept imperfection! Analyse it. Make choices and approximations. OSM has a descriptive approach, while we have functionnal questions.

		//clean small parts: need for graph analysis to detect connex components - Remove small ones
		//check connectivity: for each end node pair, compute ratio of graph distance over euclidian distance. flag/correct connectivity issues

		//is it possible to use some tags to select main lines from xxx ? examine tags and make classification
		//service? usage? maxspeed-highspeed? electrified-voltage? gauge? railway:traffic_mode? railway:preferred_direction-railway:bidirectional? name-description-ref?
		//use service! - keep only tracks with service==null (+ voltage!=null ?)

		//target: 1:50k -> Resolution 0.2mm -> 10m

		//TODO define specs based on ORM model and generalisation process in mind (at least ERM specs should be covered)
		//specs for input dataset (1:5k): tracks selected, with proper attributes, well structured. basic ETL process.
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces)
		//   leveling crossing (points)

		//see: https://gis.stackexchange.com/questions/20279/calculating-average-width-of-polygon

		//algorithm to compute average of two lines, based on curvelinear abscissa


		LOGGER.info("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/orm/";
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"shp_SE/orm_tracks.shp",3035).fs;
		//System.out.println(tracks.size()+"   "+FeatureUtil.getVerticesNumber(tracks));

		LOGGER.info("Compute graph");
		GraphBuilder.LOGGER.setLevel(Level.DEBUG);
		Graph g = GraphBuilder.build(FeatureUtil.getGeometries(tracks), null);

		//GraphSHPUtil.exportAsSHP(g, basePath+"out/", 3035);

		System.out.println("End");
	}

}
