/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;

/**
 * @author julien Gaffuri
 *
 */
public class GraphBuilder {
	public final static Logger LOGGER = Logger.getLogger(GraphBuilder.class.getName());


	//build graph from merged lines
	private static Graph build(Collection<LineString> mergedLines) {
		Graph graph = new Graph();

		if(LOGGER.isDebugEnabled()) LOGGER.debug("   Create nodes and edges");
		SpatialIndex siNodes = new Quadtree();
		for(LineString ls : mergedLines){
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

		if(LOGGER.isDebugEnabled()) LOGGER.debug("   Build face geometries with polygonisation");
		Polygonizer pg = new Polygonizer();
		pg.add(mergedLines);
		mergedLines = null;
		Collection<Polygon> polys = pg.getPolygons();
		pg = null;

		if(LOGGER.isDebugEnabled()) LOGGER.debug("   Create faces and link them to edges");
		for(Polygon poly : polys){
			//get candidate edges
			Set<Edge> edges = new HashSet<Edge>();
			Collection<Edge> es = graph.getEdgesAt(poly.getEnvelopeInternal());
			for(Edge e : es){
				Geometry edgeGeom = e.getGeometry();
				if(!edgeGeom.getEnvelopeInternal().intersects(poly.getEnvelopeInternal())) continue;

				//Geometry inter = poly.getBoundary().intersection(edgeGeom);
				//if(inter.getLength()==0) continue;

				if(!poly.covers(edgeGeom)) continue;

				edges.add(e);
			}
			//create face
			graph.buildFace(edges);
		}

		if(LOGGER.isDebugEnabled()) LOGGER.debug("Graph built ("+graph.getNodes().size()+" nodes, "+graph.getEdges().size()+" edges, "+graph.getFaces().size()+" faces)");

		return graph;
	}

	//build full graph from existing edges - NB: those edges are not kept in the new graph
	public static Graph buildFromEdges(Collection<Edge> edges) {
		Collection<LineString> mergedLines = new ArrayList<LineString>();
		for(Edge e : edges) mergedLines.add(e.getGeometry());
		return build(mergedLines);
	}

	public static Graph buildForNetworkLS(Collection<Feature> sections) {
		//for each section, create edge and link it to nodes (if it exists) or create new
		return null;
	}

	public static Graph buildForNetwork(Collection<MultiLineString> geoms) {
		if(LOGGER.isDebugEnabled()) LOGGER.debug("Build graph from "+geoms.size()+" geometries.");

		if(LOGGER.isDebugEnabled()) LOGGER.debug("     compute union of " + geoms.size() + " lines...");
		Geometry union = new GeometryFactory().buildGeometry(geoms).union();

		if(LOGGER.isDebugEnabled()) LOGGER.debug("     run linemerger...");
		LineMerger lm = new LineMerger();
		lm.add(union); union = null;
		Collection<LineString> lines = lm.getMergedLineStrings(); lm = null;
		if(LOGGER.isDebugEnabled()) LOGGER.debug("     done. " + lines.size() + " lines obtained");

		return build(lines);
	}


	public static Graph buildForTesselation(Collection<MultiPolygon> geoms) { return buildForTesselation(geoms, null); }
	public static Graph buildForTesselation(Collection<MultiPolygon> geoms, Envelope env) {
		if(LOGGER.isDebugEnabled()) LOGGER.debug("Build graph from "+geoms.size()+" geometries.");

		if(LOGGER.isDebugEnabled()) LOGGER.debug("   Run linemerger on lines");
		Collection<Geometry> lineCol = new ArrayList<Geometry>();
		for(Geometry g : geoms) lineCol.add(g.getBoundary());

		if(LOGGER.isDebugEnabled()) LOGGER.debug("     compute union of " + lineCol.size() + " lines...");
		Geometry union = null;
		GeometryFactory gf = new GeometryFactory();
		while(union == null)
			try {
				//union = new GeometryFactory().buildGeometry(lineCol);
				//union = union.union();
				union = UnaryUnionOp.union(lineCol, gf);
			} catch (TopologyException e) {
				Coordinate c = e.getCoordinate();
				LOGGER.warn("     Geometry.union failed. Topology exception (found non-noded intersection) around: " + c.x +", "+c.y);
				//LOGGER.warn("     "+e.getMessage());

				Collection<Geometry> close = JTSGeomUtil.getGeometriesCloseTo(c, lineCol, 0.001);
				Geometry unionClose = UnaryUnionOp.union(close, gf);
				lineCol.removeAll(close);
				lineCol.add(unionClose);
				union = null;
			}

		lineCol.clear(); lineCol = null;

		if(LOGGER.isDebugEnabled()) LOGGER.debug("     run linemerger...");
		LineMerger lm = new LineMerger();
		lm.add(union); union = null;
		Collection<LineString> lines = lm.getMergedLineStrings(); lm = null;
		if(LOGGER.isDebugEnabled()) LOGGER.debug("     done. " + lines.size() + " lines obtained");


		//decompose lines along the envelope (if provided)
		if(env != null) {
			Collection<LineString> lines_ = new HashSet<LineString>();
			LineString envL = JTSGeomUtil.getBoundary(env);
			for(LineString line : lines) {
				if(JTSGeomUtil.containsSFS(env, line.getEnvelopeInternal())) { lines_.add(line); continue; }
				MultiLineString inter = JTSGeomUtil.keepOnlyLinear(envL.intersection(line));
				if(inter==null || inter.isEmpty()) { lines_.add(line); continue; }
				lines_.addAll(JTSGeomUtil.getLineStringGeometries(inter));
				lines_.addAll(JTSGeomUtil.getLineStringGeometries(line.difference(inter)));
			}
			//replace collection
			lines.clear(); lines = lines_;
		}

		return build(lines);
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
