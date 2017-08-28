/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opencarto.datamodel.Feature;

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

	//absorb another face
	public Set<Edge> absorb(Face f) {
		if(f==this){
			System.err.println("Error: Cannot merge a face with itself.");
			return null;
		}

		//get edges to delete (the ones in common)
		Set<Edge> delEdges = getEdgesInCommon(f);
		if(delEdges.size()==0){
			System.err.println("Could not aggregate face "+f.getId()+" with face "+this.getId()+": No edge in common.");
			return delEdges;
		}

		Graph g = getGraph();
		boolean b=true;
		if(f.isEnclave()){
			Collection<Node> ns = f.getNodes();

			//remove face (making hole)
			g.remove(f);

			//remove hole - remove edges
			b = getEdges().removeAll(delEdges);
			if(!b) System.err.println("Error when aggregating (enclave) face "+f.getId()+" into face "+getId()+": Failed in removing edges of absorbed face "+f.getId());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; g.remove(e); }

			//remove all nodes
			for(Node n:ns) g.remove(n);
		} else {
			//store nodes concerned
			Set<Node> nodes = new HashSet<Node>();
			for(Edge delEdge : delEdges) { nodes.add(delEdge.getN1()); nodes.add(delEdge.getN2()); }

			//remove face, leaving a hole
			g.remove(f);

			//remove edges between both faces
			for(Edge e : delEdges){ e.f1=null; e.f2=null; g.remove(e); }
			b =   getEdges().removeAll(delEdges);
			if(!b) System.err.println("Error when aggregating face "+f.getId()+" into face "+getId()+": Failed in removing edges of absorbing face "+ getId()+". Nb="+delEdges.size());
			b = f.getEdges().removeAll(delEdges);
			if(!b) System.err.println("Error when aggregating face "+f.getId()+" into face "+getId()+": Failed in removing edges of absorbed face "+f.getId()+". Nb="+delEdges.size());

			//change remaining edges from absorbed face to this
			for(Edge e : f.getEdges()) if(e.f1==f) e.f1=this; else e.f2=this;
			b = getEdges().addAll(f.getEdges());
			if(!b) System.err.println("Error when aggregating face "+f.getId()+" into face "+getId()+": Failed in adding new edges to absorbing face "+getId());
			f.getEdges().clear();

			//remove single nodes
			for(Node n : nodes)
				if(n.getEdgeNumber()==0)
					g.remove(n);

			//ensure nodes are reduced, which means they do not have a degree 2
			for(Node n : nodes){
				Edge e = n.ensureReduction();
				if(e==null) continue;
				//TODO handle result of reduction: return also merged edges and add newly created edge
			}
		}
		return delEdges;
	}

}
