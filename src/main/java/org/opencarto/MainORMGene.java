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


	//get ORM data using openpass API:

	/*
(46,1.5,53,8.5)
wget -O orm.osm "http://overpass-api.de/api/map?data=[out:xml];(node[railway](46,1.5,53,8.5);way[railway](46,1.5,53,8.5);relation[railway](46,1.5,53,8.5););(._;>;);out;"
ogr2ogr --config OSM_USE_CUSTOM_INDEXING NO -skipfailures -f "ESRI Shapefile" shp orm.osm  -overwrite
	 */

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

		//target: 1:50k -> Resolution 0.2mm -> 10m

		//TODO see input data
		//define filtering based on attribute
		//Exclude:
		//"railway"=> abandoned razed disused
		//railway:preserved => no
		//historic => railway
		//"railway"=> subway funicular tram light_rail miniature
		//"attraction"
		//usage => test

		//usage => main branch industrial military tourism
		//railway:track_class => https://en.wikipedia.org/wiki/Rail_speed_limits_in_the_United_States#Track_classes

		//test connection - build topology
		//distinct main railways from secondary railways. By type? Using stroke?
		//build railway areas from locals
		//symplify main lines: collapse dual mainlines, simplify junctions, etc.
		//algorithm to compute average of two lines, based on curvelinear abscissa

		System.out.println("End");
	}

}
