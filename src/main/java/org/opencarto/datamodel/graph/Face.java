/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.opencarto.algo.base.Scaling;
import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
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
	public final static Logger LOGGER = Logger.getLogger(Face.class.getName());

	private static int ID = 0;

	Face(Graph graph){
		super(graph,"F"+(ID++));
	}

	//the edges
	private Collection<Edge> edges = new HashSet<Edge>();
	public Collection<Edge> getEdges() { return edges; }

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


	//build the geometry
	public Polygon getGeometry(){
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
		return maxPoly;
	}

	//build a feature
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

	public Collection<Node> getNodes() {
		HashSet<Node> ns = new HashSet<Node>();
		for(Edge e:getEdges()){
			ns.add(e.getN1());
			ns.add(e.getN2());
		}
		return ns;
	}

	//return edges in common between two faces (if any)
	public Set<Edge> getEdgesInCommon(Face f) {
		Set<Edge> out = new HashSet<Edge>();
		for(Edge e : getEdges()) if(e.f1==f || e.f2==f) out.add(e);
		//out.addAll(getEdges());
		//out.retainAll(f.getEdges());
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

		//scale edges' internal coordinates
		for(Edge e : getEdges()){
			for(Coordinate c : e.coords){
				if(c==e.getN1().getC()) continue;
				if(c==e.getN2().getC()) continue;
				Scaling.apply(c,center,factor);
			}
		}
		//scale nodes coordinates
		for(Node n : getNodes())
			Scaling.apply(n.getC(),center,factor);

	}

}
