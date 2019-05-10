/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.operation.union.UnaryUnionOp;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

/**
 * @author julien Gaffuri
 *
 */
public class GraphBuilder {
	public final static Logger LOGGER = Logger.getLogger(GraphBuilder.class.getName());


	/**
	 * Build graph (not necessary planar) from lines.
	 * 
	 * @param lines The input lines.
	 * @param buildFaces Set to false if the faces are not needed.
	 * @return The graph
	 */
	private static Graph build(Collection<LineString> lines, boolean buildFaces) {
		Graph g = new Graph();

		if(LOGGER.isDebugEnabled()) LOGGER.debug("   Create nodes and edges");
		SpatialIndex siNodes = new Quadtree();
		for(LineString ls : lines){
			if(ls.isClosed()) {
				Coordinate c = ls.getCoordinateN(0);
				Node n = g.getNodeAt(c);
				if(n==null) {
					n=g.buildNode(c);
					siNodes.insert(new Envelope(n.getC()), n);
				}
				Coordinate[] coords = ls.getCoordinates();
				coords[0]=n.getC(); coords[coords.length-1]=n.getC();
				g.buildEdge(n, n, coords);
			} else {
				Coordinate c;
				c = ls.getCoordinateN(0);
				Node n0 = g.getNodeAt(c);
				if(n0==null) {
					n0 = g.buildNode(c);
					siNodes.insert(new Envelope(n0.getC()), n0);
				}
				c = ls.getCoordinateN(ls.getNumPoints()-1);
				Node n1 = g.getNodeAt(c);
				if(n1==null) {
					n1 = g.buildNode(c);
					siNodes.insert(new Envelope(n1.getC()), n1);
				}
				Coordinate[] coords = ls.getCoordinates();
				coords[0]=n0.getC(); coords[coords.length-1]=n1.getC();
				g.buildEdge(n0, n1, coords);
			}
		}
		siNodes = null;

		if( !buildFaces ) {
			if(LOGGER.isDebugEnabled()) LOGGER.debug("Graph built ("+g.getNodes().size()+" nodes, "+g.getEdges().size()+" edges)");
			return g;
		}

		if(LOGGER.isDebugEnabled()) LOGGER.debug("   Build face geometries with polygonisation");
		Polygonizer pg = new Polygonizer();
		pg.add(lines);
		lines = null;
		Collection<Polygon> polys = pg.getPolygons();
		pg = null;

		if(LOGGER.isDebugEnabled()) LOGGER.debug("   Create faces and link them to edges");
		for(Polygon poly : polys){
			//get candidate edges
			Set<Edge> edges = new HashSet<Edge>();
			Collection<Edge> es = g.getEdgesAt(poly.getEnvelopeInternal());
			for(Edge e : es){
				Geometry edgeGeom = e.getGeometry();
				if(!edgeGeom.getEnvelopeInternal().intersects(poly.getEnvelopeInternal())) continue;

				//Geometry inter = poly.getBoundary().intersection(edgeGeom);
				//if(inter.getLength()==0) continue;

				if(!poly.covers(edgeGeom)) continue;

				edges.add(e);
			}
			//create face
			g.buildFace(edges);
		}

		if(LOGGER.isDebugEnabled()) LOGGER.debug("Graph built ("+g.getNodes().size()+" nodes, "+g.getEdges().size()+" edges, "+g.getFaces().size()+" faces)");

		return g;
	}

	/**
	 * Build full graph from existing edges.
	 * NB: those edges are not kept in the output graph.
	 * 
	 * @param edges
	 * @param buildFaces
	 * @return
	 */
	public static Graph buildFromEdges(Collection<Edge> edges, boolean buildFaces) {
		Collection<LineString> lines = new ArrayList<LineString>();
		for(Edge e : edges) lines.add(e.getGeometry());
		return build(lines, buildFaces);
	}

	/**
	 * Build graph from sections, by connecting them directly at their tips.
	 * This graph is not necessary planar. No faces are built.
	 * 
	 * @param sections
	 * @return
	 */
	public static Graph buildFromLinearFeaturesNonPlanar(Collection<Feature> sections) {
		Graph g = new Graph();
		for(Feature f : sections) {
			MultiLineString mls = (MultiLineString) JTSGeomUtil.toMulti(f.getGeom());
			for(int i=0; i<mls.getNumGeometries(); i++) {
				//for each section, create edge and link it to nodes (if it exists) or create new
				Coordinate[] cs = ((LineString) mls.getGeometryN(i)).getCoordinates();
				Node n1 = g.getCreateNodeAt(cs[0]), n2 = g.getCreateNodeAt(cs[cs.length-1]);
				Edge e = g.buildEdge(n1, n2, cs);
				e.obj = f;
			}
		}
		return g;
	}

	/**
	 * Build planar graph from sections.
	 * 
	 * @param sections
	 * @return
	 */
	public static Graph buildFromLinearFeaturesPlanar(Collection<Feature> sections, boolean buildFaces) {

		//build graph from geometries
		Collection<LineString> geoms = new ArrayList<LineString>();
		for(Feature f : sections) {
			Geometry g = f.getGeom();
			if(g instanceof LineString) geoms.add((LineString)g);
			else geoms.add(JTSGeomUtil.toSimple((MultiLineString)g));
		}
		Graph g = build(geoms, buildFaces);
		geoms.clear(); geoms = null;

		//link features and edges
		for(Feature f : sections)
			for(Edge e : g.getEdgesAt(f.getGeom().getEnvelopeInternal())) {
				LineString eg = e.getGeometry();
				if(!f.getGeom().getEnvelopeInternal().intersects(eg.getEnvelopeInternal())) continue;
				Geometry inter = f.getGeom().intersection(eg);
				if(inter.getLength() == 0) continue;
				//if(!f.getGeom().contains(eg) && !f.getGeom().overlaps(eg)) continue;
				if(e.obj != null)
					LOGGER.warn("Problem when building network: Ambiguous assignement of feature to edge. Around "+inter);
				e.obj = f;
			}

		//check all edges have been assigned to a feature
		for(Edge e : g.getEdges())
			if(e.obj==null) LOGGER.warn("Problem when building network: An edge has not been assigned to a feature. Around "+e.getGeometry().getCentroid().getCoordinate());

		return g;
	}

	public static Graph buildPlanar(Collection<MultiLineString> geoms, boolean buildFaces) {
		if(LOGGER.isDebugEnabled()) LOGGER.debug("Build graph from "+geoms.size()+" geometries.");

		if(LOGGER.isDebugEnabled()) LOGGER.debug("     compute union of " + geoms.size() + " lines...");
		Geometry union = new GeometryFactory().buildGeometry(geoms).union();

		if(LOGGER.isDebugEnabled()) LOGGER.debug("     run linemerger...");
		LineMerger lm = new LineMerger();
		lm.add(union); union = null;
		Collection<LineString> lines = lm.getMergedLineStrings(); lm = null;
		if(LOGGER.isDebugEnabled()) LOGGER.debug("     done. " + lines.size() + " lines obtained");

		return build(lines, buildFaces);
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

		return build(lines, true);
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
