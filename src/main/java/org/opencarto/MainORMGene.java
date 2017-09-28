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

		//http://wiki.openstreetmap.org/wiki/OpenRailwayMap
		//http://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging
		
		//See
		//https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
		//http://overpass-api.de/command_line.html

		//http://overpass.osm.rambler.ru
		//http://www.overpass-api.de
		//http://api.openstreetmap.fr

		//http://overpass-api.de/api/status

		//{{geocodeArea:luxembourg}}->.searchArea;     way["railway"="rail"](area.searchArea);

		/*
(49.6598,6.2828,49.6714,6.3165)
(49,5,51,8)
wget "http://overpass-api.de/api/map?data=[out:xml];(node[railway](49,5,51,8);way[railway](49,5,51,8);relation[railway](49,5,51,8););<;>;out;"
wget "http://overpass-api.de/api/map?data=[out:xml];node[railway](49,5,51,8);<;>;out;"
*/

		/*
 * node
  ["highway"="bus_stop"]
  ["shelter"]
  ["shelter"!="no"]
  (50.7,7.1,50.8,7.25);
out;
 * 
*/	
		
		//target: 1:50k -> Resolution 0.2mm -> 10m
		//filtering: remove by type, etc.
		//test connection
		//distinct mainlines from local. By type? Using stroke?
		//build areas from locals
		//symplify main lines: collapse dual mainlines, simplify junctions, etc.

		System.out.println("End");
	}

}
