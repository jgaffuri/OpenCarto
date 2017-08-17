package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * Valued and oriented graph.
 * 
 * @author julien Gaffuri
 *
 */
public class Graph{

	//the nodes
	private Collection<Node> nodes = new HashSet<Node>();
	public Collection<Node> getNodes() { return nodes; }

	//build a node
	public Node buildNode(Coordinate c){
		Node n = new Node(this,c);
		nodes.add(n);
		spIndNode.insert(new Envelope(n.getC()), n);
		return n;
	}


	//the edges
	private Collection<Edge> edges = new HashSet<Edge>();
	public Collection<Edge> getEdges() { return edges; }

	//build an edge
	public Edge buildEdge(Node n1, Node n2){ return buildEdge(n1,n2,null); }
	public Edge buildEdge(Node n1, Node n2, Coordinate[] coords){
		Edge e = new Edge(this, n1,n2,coords);
		edges.add(e);
		spIndEdge.insert(e.getGeometry().getEnvelopeInternal(), e);
		return e;
	}



	//the faces
	private Collection<Face> faces = new HashSet<Face>();
	public Collection<Face> getFaces() { return faces; }

	//build a graph face
	public Face buildFace() {
		Face f = new Face(this);
		faces.add(f);
		return f;
	}



	//Remove a node from the graph. The node is supposed not to be linked to any edge.
	public void remove(Node n) {
		boolean b;
		b = nodes.remove(n);
		if(!b) System.err.println("Error when removing node "+n.getId()+". Not in graph nodes list.");
		b = spIndNode.remove(new Envelope(n.getC()), n);
		if(!b) System.err.println("Error when removing node "+n.getId()+". Not in spatial index.");
		if(n.getEdges().size()>0) System.err.println("Error when removing node "+n.getId()+". Edges are still linked to it (nb="+n.getEdges().size()+")"); //+ "   "+n.getEdges().iterator().next().getId());
		if(n.getFaces().size()>0) System.err.println("Error when removing node "+n.getId()+". Faces are still linked to it (nb="+n.getFaces().size()+")");
	}

	//Remove an edge from a graph. The edge is supposed not to be linked to any face.
	public void remove(Edge e) {
		boolean b;
		b = edges.remove(e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+". Not in graph edges list.");
		b = spIndEdge.remove(e.getGeometry().getEnvelopeInternal(), e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+". Not in spatial index.");
		b = e.getN1().getOutEdges().remove(e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+". Not in N1 out edges");
		b = e.getN2().getInEdges().remove(e);
		if(!b) System.err.println("Error when removing edge "+e.getId()+". Not in N2 in edges");
		if(e.f1 != null) System.err.println("Error when removing edge "+e.getId()+". It is still linked to face "+e.f1);
		if(e.f2 != null) System.err.println("Error when removing edge "+e.getId()+". It is still linked to face "+e.f2);
	}
	public void removeAll(Collection<Edge> es) { for(Edge e:es) remove(e); }


	public void removeFace(Face f) {
		boolean b;

		//remove face from list
		b = getFaces().remove(f);
		if(!b) System.err.println("Could not remove face "+f.getId()+" from graph");

		//break link with edges
		for(Edge e:f.getEdges()){
			if(e.f1==f) e.f1=null;
			else if(e.f2==f) e.f2=null;
			else System.err.println("Could not remove link between face "+f.getId()+" and edge "+e.getId());
		}

	}


	//support for spatial queries

	private SpatialIndex spIndNode = new Quadtree();
	public SpatialIndex getSpatialIndexNode() { return spIndNode; }
	private SpatialIndex spIndEdge = new Quadtree();
	public SpatialIndex getSpatialIndexEdge() { return spIndEdge; }

	/*public Quadtree getNodeSpatialIndex(){
		Quadtree si = new Quadtree();
		for(Node n : getNodes()) si.insert(new Envelope(n.getC()), n);
		return si;
	}*/

	public Node getNodeAt(Coordinate c) {
		Envelope env = new Envelope(c);
		//env.expandBy(5);
		List<?> elts = spIndNode.query(env);
		for(Object elt : elts){
			Node n = (Node)elt;
			if(c.distance(n.getC()) == 0) return n;
		}
		return null;
	}


	/*public Quadtree getEdgeSpatialIndex(){
		Quadtree si = new Quadtree();
		for(Edge e : getEdges()) si.insert(e.getGeometry().getEnvelopeInternal(), e);
		return si;
	}*/

	public List<Edge> getEdgesAt(Envelope env) {
		return spIndEdge.query(env);
	}





	public Collection<Feature> getFaceFeatures(int epsg){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(Face face:getFaces()) {
			Feature f = face.toFeature();
			if(f.getGeom()==null){
				System.out.println("NB: null geom for face "+face.getId());
				continue;
			}
			if(!f.getGeom().isValid()) {
				System.out.println("NB: non valide geometry for face "+face.getId());
				continue;
			}
			f.setProjCode(epsg);
			fs.add(f);
		}
		return fs;
	}

	public Collection<Feature> getEdgeFeatures(int epsg){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(Edge e:getEdges()){
			Feature f = e.toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		return fs;		
	}

	public Collection<Feature> getNodeFeatures(int epsg){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(Node n:getNodes()) {
			Feature f = n.toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		return fs;		
	}

	//merge two edges into a single one
	//TODO better look at case of edge merging resulting in a closed edge?
	/*public Edge merge(Edge e1, Edge e2) {
		if(e1.getN1()==e2.getN2() && e1.getN2()!=e2.getN1()) return merge(e2,e1);
		if(e1.getN1()==e2.getN1()) return merge(e1.revert(),e2);
		if(e1.getN2()==e2.getN2()) return merge(e1,e2.revert());

		//get nodes
		Node n1=e1.getN1(), n=e1.getN2(), n2=e2.getN2();

		//build new edge geometry
		Coordinate[] coords = new Coordinate[e1.coords.length + e2.coords.length - 1];
		for(int i=0; i<e1.coords.length; i++) coords[i]=e1.coords[i];
		for(int i=e1.coords.length; i<e1.coords.length + e2.coords.length - 1; i++) coords[i]=e2.coords[i-e1.coords.length];

		//build new edge
		Edge e = buildEdge(n1, n2, coords);

		//link new edge to faces
		Set<Face> faces_ = new HashSet<Face>();
		faces_.addAll(e1.getFaces()); faces_.addAll(e2.getFaces());
		if(faces_.size()>2) System.err.println("Could not merge edges "+e1.getId()+" and "+e2.getId()+": Unexpected number of faces: "+faces_.size()+". Should be 2, maximum.");
		Iterator<Face> it = faces_.iterator();
		if(it.hasNext()) e.f1=it.next();
		if(it.hasNext()) e.f2=it.next();

		//link faces to new edge
		for(Face f:faces_) f.getEdges().add(e);

		//break faces link to initial edges
		if(e1.f1!=null) { e1.f1.getEdges().remove(e1); e1.f1=null; }
		if(e1.f2!=null) { e1.f2.getEdges().remove(e1); e1.f2=null; }
		if(e2.f1!=null) { e2.f1.getEdges().remove(e2); e2.f1=null; }
		if(e2.f2!=null) { e2.f2.getEdges().remove(e2); e2.f2=null; }

		//delete edges and middle node
		remove(e1); remove(e2); remove(n);

		return e;
	}*/

}
