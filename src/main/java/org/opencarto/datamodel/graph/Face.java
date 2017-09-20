/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.algo.base.Scaling;
import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * A graph face.
 * It is defined by a set of graph edges.
 * Its geometry is a Polygon, possibly with holes.
 * 
 * @author julien Gaffuri
 *
 */
public class Face extends GraphElement{
	private final static Logger LOGGER = Logger.getLogger(Face.class);

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
	public Polygon getGeometry() { return geom; }

	public void updateGeometry() {
		//remove current geometry from spatial index
		if(geom != null && !geom.isEmpty()) {
			boolean b = getGraph().getSpatialIndexFace().remove(geom.getEnvelopeInternal(), this);
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
			getGraph().getSpatialIndexFace().insert(geom.getEnvelopeInternal(), this);
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
	public boolean isOK() {
		Polygon g = getGeometry();

		if(g==null) return false;
		if(g.isEmpty()) return false;
		//if(!g.isValid()) return false; //unnecessary, since it is also tested in isSimple() method
		//if(!g.isSimple()) return false;

		//check face does not overlap other faces
		Envelope env = g.getEnvelopeInternal();
		for(Object f2_ : getGraph().getSpatialIndexFace().query(env)){
			Face f2 = (Face)f2_;
			if(this==f2) continue;
			Polygon g2 = f2.getGeometry();

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

	//scale a face
	public void scale(double factor) {
		if(factor == 1) return;

		//get center
		Coordinate center = getGeometry().getCentroid().getCoordinate();

		//remove all edges from spatial index
		boolean b;
		for(Edge e : getEdges()){
			b = getGraph().getSpatialIndexEdge().remove(e.getGeometry().getEnvelopeInternal(), e);
			if(!b) LOGGER.error("Could not remove edge from spatial index when scaling face");
		}

		//scale edges' internal coordinates
		for(Edge e : getEdges()){
			for(Coordinate c : e.getCoords()){
				if(c==e.getN1().getC()) continue;
				if(c==e.getN2().getC()) continue;
				Scaling.apply(c,center,factor);
			}
		}

		//scale nodes coordinates
		for(Node n : getNodes())
			Scaling.apply(n.getC(),center,factor);

		//add edges to spatial index with new geometry
		for(Edge e : getEdges())
			getGraph().getSpatialIndexEdge().insert(e.getGeometry().getEnvelopeInternal(), e);

		//force geometry update
		updateGeometry();
		for(Face f : getTouchingFaces())
			f.updateGeometry();
	}

	//return face as a feature
	public Feature toFeature() {
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.id=getId();
		f.getProperties().put("id", getId());
		f.getProperties().put("value", value);
		f.getProperties().put("edge_nb", getEdges().size());
		String txt=null;
		for(Edge e:getEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.getProperties().put("edge", txt);
		f.getProperties().put("type", getType());
		f.getProperties().put("face_nb", getTouchingFaces().size());
		return f;
	}

}
