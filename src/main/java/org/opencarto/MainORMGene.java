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


	//TODO make import script
	//TODO do filtering at import level
	//http://www.overpass-api.de/query_form.html

	//improve request mechanism


	//doc:
	//http://wiki.openstreetmap.org/wiki/OpenRailwayMap  ---  http://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging
	//https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
	//https://stackoverflow.com/questions/31879288/overpass-api-way-coordinates

	//http://www.overpass-api.de/api/
	//http://overpass.osm.rambler.ru/cgi/
	//http://api.openstreetmap.fr/api/

	//http://www.overpass-api.de/api/status


	public static void main(String[] args) {
		System.out.println("Start");

		//TODO define specs based on ORM model and generalisation process in mind (at least ERM specs should be covered)
		//specs for input dataset (1:5k): tracks selected, with proper attributes, well structured. basic ETL process.
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces)
		//   leveling crossing (points)
		//target: 1:50k -> Resolution 0.2mm -> 10m


		//TODO see input data
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
