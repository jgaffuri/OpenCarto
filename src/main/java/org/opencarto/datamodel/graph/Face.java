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
	}

	//the edges
	private Set<Edge> edges;
	public Set<Edge> getEdges() { return edges; }

	//the geometry, derived from edges geometries with polygoniser
	private Polygon geom = null;
	boolean geomUpdateNeeded = true;

	public Polygon getGeometry(){
		if(geomUpdateNeeded) updateGeometry();
		return geom;
	}

	public void updateGeometry(){
		//remove current geometry from spatial index
		boolean b;
		if(geom != null){
			b = getGraph().getSpatialIndexFace().remove(geom.getEnvelopeInternal(), this);
			if(!b) LOGGER.error("Could not remove face "+this.getId()+" from spatial index when updating its geometry.");
		}

		//build new geometry with polygoniser
		Polygonizer pg = new Polygonizer();
		for(Edge e : edges) pg.add(e.getGeometry());
		Collection<Polygon> polys = pg.getPolygons();
		pg = null;

		//if(polys.size() == 1) return polys.iterator().next();

		//return polygon whose external ring has the largest area
		double maxArea = -1; Polygon maxPoly = null;
		for(Polygon poly : polys){
			double area = poly.getEnvelopeInternal().getArea();
			if(area > maxArea){
				maxArea = area;
				maxPoly = poly;
			}
		}

		//set geometry
		geom = maxPoly;

		if(geom != null)
			//update index
			getGraph().getSpatialIndexFace().insert(geom.getEnvelopeInternal(), this);

		geomUpdateNeeded=false;
	}

	public Collection<Face> getTouchingFaces(){
		Collection<Face> out = new HashSet<Face>();
		for(Edge e:getEdges()) out.addAll(e.getFaces());
		out.remove(this);
		return out;
	}

	public boolean isEnclave(){
		if(isCoastal()) return false;
		return getTouchingFaces().size()==1;
	}
	public boolean isIsland(){ return getTouchingFaces().size()==0; }
	//public boolean isEnclave(){ return edges.size()==1 && edges.iterator().next().getFaces().size()==2; }
	//public boolean isIsland(){ return edges.size()==1 && edges.iterator().next().getFaces().size()==1; }
	public boolean isCoastal(){
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


	//check the face is valid, that is: its geometry is simple & valid and it does not overlap other faces
	public boolean isValid(){
		Polygon g = getGeometry();

		if(g==null) return false;
		if(g.isEmpty()) return false;
		if(!g.isValid()) return false;
		if(!g.isSimple()) return false;

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
		geomUpdateNeeded = true;
	}



	//return face as a feature
	public Feature toFeature(){
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
