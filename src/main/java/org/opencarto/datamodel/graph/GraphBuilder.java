/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * @author julien Gaffuri
 *
 */
public class GraphBuilder {




	public static Graph build(Collection<MultiPolygon> units) {
		System.out.println("Build graph from "+units.size()+" units.");

		Graph graph = new Graph();

		System.out.println("   Run linemerger on rings");
		Collection<Geometry> lineCol = new HashSet<Geometry>();
		for(MultiPolygon unit : units) lineCol.add(unit.getBoundary());
		System.out.println("     compute union of boundaries...");
		Geometry un = new GeometryFactory().buildGeometry(lineCol).union();
		System.out.println("     linemerger...");
		LineMerger lm = new LineMerger();
		lm.add(un);
		un = null;
		Collection<LineString> lines = lm.getMergedLineStrings();
		lm = null;


		System.out.println("   Create nodes and edges");
		SpatialIndex siNodes = new Quadtree();
		for(LineString ls : lines){
			if(ls.isClosed()) {
				Coordinate c = ls.getCoordinateN(0);
				Node n = graph.getNodeAt(c);
				if(n==null) {
					n=graph.buildNode(c);
					siNodes.insert(new Envelope(n.getC()), n);
				}
				Coordinate[] coords = ls.getCoordinates();
				coords[0]=n.getC(); coords[coords.length-1]=n.getC();
				graph.buildEdge(n, n, coords);
			} else {
				Coordinate c;
				c = ls.getCoordinateN(0);
				Node n0 = graph.getNodeAt(c);
				if(n0==null) {
					n0 = graph.buildNode(c);
					siNodes.insert(new Envelope(n0.getC()), n0);
				}
				c = ls.getCoordinateN(ls.getNumPoints()-1);
				Node n1 = graph.getNodeAt(c);
				if(n1==null) {
					n1 = graph.buildNode(c);
					siNodes.insert(new Envelope(n1.getC()), n1);
				}
				Coordinate[] coords = ls.getCoordinates();
				coords[0]=n0.getC(); coords[coords.length-1]=n1.getC();
				graph.buildEdge(n0, n1, coords);
			}
		}
		siNodes = null;

		System.out.println("   Build face geometries with polygonisation");
		Polygonizer pg = new Polygonizer();
		pg.add(lines);
		lines = null;
		Collection<Polygon> polys = pg.getPolygons();
		pg = null;

		System.out.println("   Create faces and link them to edges");
		for(Polygon poly : polys){
			Face d = graph.buildFace();
			//get candidate edges
			Collection<Edge> es = graph.getEdgesAt(poly.getEnvelopeInternal());
			for(Edge e : es){
				Geometry edgeGeom = e.getGeometry();
				if(!edgeGeom.getEnvelopeInternal().intersects(poly.getEnvelopeInternal())) continue;

				//Geometry inter = poly.getBoundary().intersection(edgeGeom);
				//if(inter.getLength()==0) continue;

				if(!poly.covers(edgeGeom)) continue;

				d.getEdges().add(e);
				if(e.f1==null) e.f1=d; else e.f2=d;
			}
		}

		System.out.println("Graph built ("+graph.getNodes().size()+" nodes, "+graph.getEdges().size()+" edges, "+graph.getFaces().size()+" faces)");

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

		//TODO build faces from mpps

		return graph;
	}*/




}
