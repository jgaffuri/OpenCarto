package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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
public class Graph {
	public final static Logger LOGGER = Logger.getLogger(Graph.class.getName());

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


	public void remove(Face f) {
		boolean b;

		//remove face from list
		b = getFaces().remove(f);
		if(!b) System.err.println("Could not remove face "+f.getId()+" from graph");

		//break link with edges
		for(Edge e : f.getEdges()){
			if(e.f1==f) e.f1=null;
			else if(e.f2==f) e.f2=null;
			else System.err.println("Could not remove link between face "+f.getId()+" and edge "+e.getId());
		}

		//unnecessary
		//f.getEdges().clear();
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




	//aggregate two faces
	public Set<Edge> aggregate(Face fTarget, Face delFace) {
		if(delFace==fTarget){
			System.err.println("Error: Cannot aggregate a face with itself.");
			return null;
		}

		//get edges to delete (the ones in common)
		Set<Edge> delEdges = fTarget.getEdgesInCommon(delFace);
		if(delEdges.size()==0){
			System.err.println("Could not aggregate face "+delFace.getId()+" with face "+fTarget.getId()+": No edge in common.");
			return delEdges;
		}

		boolean b=true;
		if(delFace.isEnclave()){
			Collection<Node> ns = delFace.getNodes();

			//remove face (making hole)
			remove(delFace);

			//remove hole - remove edges
			b = fTarget.getEdges().removeAll(delEdges);
			if(!b) System.err.println("Error when aggregating (enclave) face "+delFace.getId()+" into face "+fTarget.getId()+": Failed in removing edges of absorbed face "+delFace.getId());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; remove(e); }

			//remove all nodes
			for(Node n:ns) remove(n);
		} else {
			//store nodes concerned
			Set<Node> nodes = new HashSet<Node>();
			for(Edge delEdge : delEdges) { nodes.add(delEdge.getN1()); nodes.add(delEdge.getN2()); }

			//remove face, leaving a hole
			remove(delFace);

			//remove edges between both faces
			for(Edge e : delEdges){ e.f1=null; e.f2=null; remove(e); }
			b = fTarget.getEdges().removeAll(delEdges);
			if(!b) System.err.println("Error when aggregating face "+delFace.getId()+" into face "+fTarget.getId()+": Failed in removing edges of absorbing face "+ fTarget.getId()+". Nb="+delEdges.size());
			b = delFace.getEdges().removeAll(delEdges);
			if(!b) System.err.println("Error when aggregating face "+delFace.getId()+" into face "+fTarget.getId()+": Failed in removing edges of absorbed face "+delFace.getId()+". Nb="+delEdges.size());

			//change remaining edges from absorbed face to this
			for(Edge e : delFace.getEdges()) if(e.f1==delFace) e.f1=fTarget; else e.f2=fTarget;
			b = fTarget.getEdges().addAll(delFace.getEdges());
			if(!b) System.err.println("Error when aggregating face "+delFace.getId()+" into face "+fTarget.getId()+": Failed in adding new edges to absorbing face "+fTarget.getId());
			delFace.getEdges().clear();

			//remove single nodes
			for(Node n : nodes)
				if(n.getEdgeNumber()==0)
					remove(n);

			//ensure nodes are reduced, which means they do not have a degree 2
			for(Node n : nodes){
				Edge e = n.ensureReduction();
				if(e==null) continue;
				delEdges.add(e);
			}
		}
		return delEdges;
	}




	//merge two edges into a new single one
	public Edge merge(Edge e1, Edge e2) {
		if(e1.isClosed() || e2.isClosed()){
			System.err.println("Cannot merge edges if one is closed.");
			return null;
		}

		//"closed" case
		if(e1.getN1()==e2.getN1() && e1.getN2()==e2.getN2()) merge(e1.revert(),e2);
		//handle other cases
		else if(e1.getN1()==e2.getN2()) return merge(e2,e1);
		else if(e1.getN1()==e2.getN1()) return merge(e1.revert(),e2);
		else if(e1.getN2()==e2.getN2()) return merge(e1,e2.revert());

		return null;

		/*/get nodes
		Node n=e1.getN2(), n2=e2.getN2();

		//build new edge geometry
		Coordinate[] coords = new Coordinate[e1.coords.length + e2.coords.length - 1];
		for(int i=0; i<e1.coords.length; i++) coords[i] = e1.coords[i];
		for(int i=e1.coords.length; i<e1.coords.length + e2.coords.length - 1; i++) coords[i] = e2.coords[i-e1.coords.length+1];

		//disconnect and remove e2
		if(e2.f1!=null) { e2.f1.getEdges().remove(e2); e2.f1=null; }
		if(e2.f2!=null) { e2.f2.getEdges().remove(e2); e2.f2=null; }
		remove(e2);

		//remove middle node
		remove(n);

		//update e1 with new geometry and new final node
		e1.setGeom(new GeometryFactory().createLineString(coords));
		e1.setN2(n2);

		return e2;*/
	}

}
