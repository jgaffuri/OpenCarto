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

		//See
		//https://blog-en.openalfa.com/how-to-query-openstreetmap-using-the-overpass-api
		//http://overpass-api.de/command_line.html

		//http://overpass.osm.rambler.ru
		//http://www.overpass-api.de
		//http://api.openstreetmap.fr

		//http://overpass-api.de/api/status
		//http://overpass-api.de/api/interpreter?data=node[name=\"Gielgen\"];out;
		//http://overpass-api.de/api/map?bbox=6.2828,49.6598,6.3165,49.6714&data=node[railway];out;
		//http://overpass-api.de/api/map?bbox=6.2828,49.6598,6.3165,49.6714&data=way[railway];out;

		
		//{{geocodeArea:luxembourg}}->.searchArea;
		//way["railway"="rail"](area.searchArea);

		/*BBOX
		node(50.745,7.17,50.75,7.18);out;
		(
				node(50.745,7.17,50.75,7.18);
				way(50.745,7.17,50.75,7.18);
				relation(50.745,7.17,50.75,7.18);
				);
				out;
		 */
/*
(49.6598,6.2828,49.6714,6.3165)
(49,5,51,8)
http://overpass-api.de/api/map?data=[out:xml];(node[railway](49,5,51,8);way[railway](49,5,51,8);relation[railway](49,5,51,8););>;out;
http://overpass-api.de/api/map?data=[out:xml];node[railway](49,5,51,8);<;>;out;
wget
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
