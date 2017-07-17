/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.List;

import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * @author julien Gaffuri
 *
 */
public class GraphBuilder {


	
	
	
	/*public static Graph buildNetwork(List<MultiLineString> mlss){ return buildNetwork(mlss,null); }
	public static Graph buildNetwork(List<MultiLineString> mlss, List<Object> objs){ return buildNetwork(mlss,objs,0); }
	public static Graph buildNetwork(List<MultiLineString> mlss, List<Object> objs, double resolution){
		Graph graph = new Graph();

		//TODO make collection of nodes from start/end coordinates - using resolution
		//TODO spatial indexes them
		//TODO for each mls component, get nodes and make edge - link object (if any).

		return graph;
	}


	public static Graph buildPartition(List<MultiPolygon> mpss){ return buildPartition(mpss,null); }
	public static Graph buildPartition(List<MultiPolygon> mpss, List<Object> objs){ return buildPartition(mpss,objs,0); }
	public static Graph buildPartition(List<MultiPolygon> mpss, List<Object> objs, double resolution){
		Graph graph = new Graph();

		//TODO make collection of nodes from start/end coordinates - using resolution
		//TODO spatial indexes them
		//TODO for each mpps component, go along each ring and build edges from nodes
		//TODO etc.

		return graph;
	}*/


	//NUTS case: regions and boundaries
	/*public static Graph buildPartition(List<MultiLineString> mlss, List<Object> objBNs, List<MultiPolygon> mpss, List<Object> objRGs, double resolution){

		//build network from edges
		Graph graph = buildNetwork(mlss,objBNs,resolution);

		//TODO build domains from mpps

		return graph;
	}*/




	//get all unique coordinates of a geometry
	/*private static Collection<Coordinate> getUniqueCoordinates(Geometry geom) {
		Quadtree qt = new Quadtree();
		//TODO
		Collection<Coordinate> out = new HashSet<Coordinate>();
		return out;
	}

	private static HashMap<Coordinate,Integer> getSeveralOverCoords(Collection<Geometry> geoms, double resolution){
		for(Geometry geom : geoms){
			Collection<Coordinate> cs = getUniqueCoordinates(geom);
		}

		HashMap<Coordinate,Integer> out = new HashMap<Coordinate,Integer>();

		return out;
	}*/

}
