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

		//http://overpass.osm.rambler.ru
		//http://www.overpass-api.de
		//http://api.openstreetmap.fr

		//http://overpass-api.de/api/status
		//http://overpass-api.de/api/interpreter?data=node[name=\"Gielgen\"];out;
		//http://overpass-api.de/api/map?bbox=6.2828,49.6598,6.3165,49.6714&data=node[railway];out;
		//http://overpass-api.de/api/map?bbox=6.2828,49.6598,6.3165,49.6714&data=way[railway];out;
		//http://overpass-api.de/api/interpreter?data=node(1422314245);out;

		//BBOX
		//node(50.745,7.17,50.75,7.18);out;


		//target: 1:50k -> Resolution 0.2mm -> 10m
		//filtering: remove by type, etc.
		//test connection
		//distinct mainlines from local. By type? Using stroke?
		//build areas from locals
		//symplify main lines: collapse dual mainlines, simplify junctions, etc.

		System.out.println("End");
	}

}
