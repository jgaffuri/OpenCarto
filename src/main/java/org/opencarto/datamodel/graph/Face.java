/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

/**
 * A graph face.
 * It is defined by a set of graph edges.
 * Its geometry is a Polygon, possibly with holes.
 * 
 * @author julien Gaffuri
 *
 */
public class Face extends GraphElement{
	private final static Logger LOGGER = Logger.getLogger(Face.class.getName());

	private static int ID = 0;

	Face(Graph graph, Set<Edge> edges){
		super(graph,"F"+(ID++));
		this.edges = edges;
		updateGeometry();
	}

	//the edges
	private Set<Edge> edges;
	public Set<Edge> getEdges() { return edges; }

	//the geometry, derived from edges geometries with polygoniser
	private Polygon geom = null;
	public Polygon getGeom() { return geom; }

	public void updateGeometry() {
		//remove current geometry from spatial index
		if(geom != null && !geom.isEmpty()) {
			boolean b = getGraph().removeFromSpatialIndex(this);
			if(!b) LOGGER.warn("Could not remove face "+this.getId()+" from spatial index when updating its geometry. NbPoints="+geom.getCoordinates().length);
		}

		geom = null;

		if(getEdges().size() == 0) return;

		//build new geometry with polygoniser
		Polygonizer pg = new Polygonizer();
		for(Edge e : edges) pg.add(e.getGeometry());
		Collection<Polygon> polys = pg.getPolygons();
		pg = null;

		//get polygon whose enveloppe has the largest area
		double maxArea = -1;
		for(Polygon poly : polys){
			double area = poly.getEnvelopeInternal().getArea();
			if(area < maxArea)
				continue;
			else if(area > maxArea) {
				maxArea = area;
				geom = poly;
			} else if(area == maxArea && poly.getArea() > geom.getArea()){
				geom = poly;
				//LOGGER.warn("Ambiguity to compute polygonal geometry of "+getId()+" with polygonisation of edges: 2 candidates geometries where found.");
			}
		}

		if(geom == null || geom.isEmpty())
			;//LOGGER.warn("Could not build geometry with polygonisation for face "+getId());
		else
			//update index
			getGraph().insertInSpatialIndex(this);
	}

	public Collection<Face> getTouchingFaces() {
		Collection<Face> out = new HashSet<Face>();
		for(Edge e:getEdges()) out.addAll(e.getFaces());
		out.remove(this);
		return out;
	}

	public boolean isEnclave() {
		if(isCoastal()) return false;
		return getTouchingFaces().size()==1;
	}
	public boolean isIsland() { return getTouchingFaces().size()==0; }
	//public boolean isEnclave(){ return edges.size()==1 && edges.iterator().next().getFaces().size()==2; }
	//public boolean isIsland(){ return edges.size()==1 && edges.iterator().next().getFaces().size()==1; }
	public boolean isCoastal() {
		for(Edge e:getEdges()) if(e.isCoastal()) return true;
		return false;
	}

	public String getType() {
		if(isEnclave()) return "enclave";
		if(isIsland()) return "island";
		if(isCoastal()) return "coastal";
		return "normal";
	}





	public Collection<Node> getNodes() {
		HashSet<Node> ns = new HashSet<Node>();
		for(Edge e:getEdges()){
			ns.add(e.getN1());
			ns.add(e.getN2());
		}
		return ns;
	}


	//check the face is ok, that is: its geometry is "simple" (no self adjency and internal ring are inside) and it does not overlap other faces
	public boolean isOK(boolean checkIsSimple, boolean checkFaceToFaceOverlap) {
		Polygon g = getGeom();

		if(g == null || g.isEmpty()) return false;

		//if(!g.isValid()) return false; //unnecessary, since it is also tested in isSimple() method
		if(checkIsSimple && !g.isSimple()) return false;

		if(checkFaceToFaceOverlap){
			//check face does not overlap other faces
			Envelope env = g.getEnvelopeInternal();
			for(Face f2 : getGraph().getFacesAt(env)){
				if(this==f2) continue;
				Polygon g2 = f2.getGeom();

				if(g2==null || g2.isEmpty()) {
					LOGGER.warn("Null/empty geometry found for face "+f2.getId());
					continue;
				}
				if(!g2.getEnvelopeInternal().intersects(env)) continue;

				try {
					//if(!g2.intersects(g)) continue;
					//if(g2.touches(g)) continue;
					if(!g2.overlaps(g)) continue;
					return false;
				} catch (Exception e){ return false; }
			}
		}

		return true;
	}

	//return edges in common between two faces (if any)
	public Set<Edge> getEdgesInCommon(Face f) {
		Set<Edge> out = new HashSet<Edge>();
		for(Edge e : f.getEdges()) if(e.f1==this || e.f2==this) out.add(e);
		return out;
	}

	//return the length of the boundary between two faces
	public double getLength(Face f) {
		double length = 0;
		for(Edge e:getEdgesInCommon(f))
			length += e.getGeometry().getLength();
		return length;
	}

	//determine best surrounding face to aggregate with: the surrounding face with the longest boundary
	//TODO improve candidate selection method (maybe the other face's size could also be considered?)
	//TODO propose also face collapse if several equivalent candidates are found.
	public Face getBestAggregationCandidate() {
		Face bestCandidateFace = null;
		double maxLength=-1;
		for(Face f2 : this.getTouchingFaces()){
			double length = this.getLength(f2);
			if(length<maxLength) continue;
			bestCandidateFace = f2; maxLength = length;
		}
		return bestCandidateFace;
	}


	//get longest edge
	public Edge getLongestEdge() {
		Edge eMax = null; double lMax = -1;
		for(Edge e : getEdges()) {
			double l = e.getGeometry().getLength();
			if(l>lMax) { eMax = e;  lMax = l; }
		}
		return eMax;
	}





	public Face clear() {
		if(getEdges() != null) getEdges().clear();
		geom = null;
		return this;
	}

}
