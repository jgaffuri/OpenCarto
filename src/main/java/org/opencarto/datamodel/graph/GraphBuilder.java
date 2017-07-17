/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * @author julien Gaffuri
 *
 */
public class GraphBuilder {


	

	public static Graph build(Collection<MultiPolygon> units) {
		Graph graph = new Graph();

		//use jts linemerger on rings
		//create nodes and edges from retur




		return graph;
	}



	/*/get all unique coordinates used in a geometry
	private static Collection<Coordinate> getUniqueCoordinates(Geometry geom) {
		Collection<Coordinate> out = new HashSet<Coordinate>();
		Quadtree qt = new Quadtree();
		for(Coordinate c : geom.getCoordinates()){

			List<Coordinate> cs_ = qt.query(new Envelope(c));
			boolean found=false;
			for(Coordinate c_ : cs_) if(c_.distance(c) == 0) found=true;
			if(found) continue;

			qt.insert(new Envelope(c), c);
			out.add(c);
		}
		return out;
	}*/





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




}
