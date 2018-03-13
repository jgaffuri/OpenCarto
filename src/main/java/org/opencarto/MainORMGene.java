/**
 * 
 */
package org.opencarto;

/**
 * 
 * Some tests on ORM generalisation
 * 
 * @author julien Gaffuri
 *
 */
public class MainORMGene {

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

		//is it possible to use some tags to select main lines from xxx ?
		//see tags - to make classification
		//service? usage? maxspeed-highspeed? electrified-voltage? gauge? railway:traffic_mode? railway:preferred_direction-railway:bidirectional? name-description-ref?
		//use service! - keep only tracks with service==null + voltage!=null

		//TODO define specs based on ORM model and generalisation process in mind (at least ERM specs should be covered)
		//specs for input dataset (1:5k): tracks selected, with proper attributes, well structured. basic ETL process.
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces)
		//   leveling crossing (points)
		//target: 1:50k -> Resolution 0.2mm -> 10m

		//see: https://gis.stackexchange.com/questions/20279/calculating-average-width-of-polygon

		//TODO see input ORM data
		//http://wiki.openstreetmap.org/wiki/OpenRailwayMap
		//http://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging
		
		//define filtering based on attribute
		//Exclude:
		//NO "railway"=> abandoned razed disused
		//NO railway:preserved => no
		//NO historic => railway
		//"railway"=> subway funicular tram light_rail miniature
		//"attraction"
		//usage => test

		//tags helping for generalisation:
		//usage => main branch industrial military tourism
		//on not main: service => yard spur siding crossover
		//railway:track_class => https://en.wikipedia.org/wiki/Rail_speed_limits_in_the_United_States#Track_classes
		//maxspeed

		//direction: See railway:preferred_direction and railway:bidirectional

		//test connection - build topology
		//generalisation: derive railway lines from railway tracks
		//distinct 'main' lines from 'branch' lines. By type? Using stroke?
		//build railway areas from locals
		//symplify main lines: collapse dual mainlines, simplify junctions, etc.
		//algorithm to compute average of two lines, based on curvelinear abscissa

		System.out.println("End");
	}

}
