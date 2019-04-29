package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.opencarto.algo.distances.HausdorffDistance;
import org.opencarto.datamodel.Feature;

/**
 * Valued and oriented graph.
 * 
 * @author julien Gaffuri
 *
 */
public class Graph {
	private final static Logger LOGGER = Logger.getLogger(Graph.class.getName());

	//the nodes
	private Set<Node> nodes = new HashSet<Node>();
	public Set<Node> getNodes() { return nodes; }

	//build a node
	public Node buildNode(Coordinate c){
		Node n = new Node(this,c);
		nodes.add(n);
		return n;
	}


	//the edges
	private Set<Edge> edges = new HashSet<Edge>();
	public Set<Edge> getEdges() { return edges; }

	//build an edge
	public Edge buildEdge(Node n1, Node n2){ return buildEdge(n1, n2, null); }
	public Edge buildEdge(Node n1, Node n2, Coordinate[] coords){
		Edge e = new Edge(this, n1, n2, coords);
		edges.add(e);
		return e;
	}



	//the faces
	private Set<Face> faces = new HashSet<Face>();
	public Set<Face> getFaces() { return faces; }

	//build a graph face
	public Face buildFace(Set<Edge> edges) {
		Face f = new Face(this, edges);
		for(Edge e : edges){
			if(e.f1==null) e.f1=f;
			else if(e.f2==null) e.f2=f;
			else LOGGER.error("Error when building face "+f.getId()+". Edge "+e.getId()+" is already linked to two faces: "+e.f1.getId()+" and "+e.f2.getId());
		}
		faces.add(f);
		return f;
	}



	//Remove a node from the graph. The node is supposed not to be linked to any edge.
	public void remove(Node n) {
		boolean b;
		b = nodes.remove(n);
		if(!b) LOGGER.error("Error when removing node "+n.getId()+". Not in graph nodes list. Position="+n.getC());

		b = removeFromSpatialIndex(n);
		if(!b) LOGGER.error("Error when removing node "+n.getId()+". Not in spatial index. Position="+n.getC());

		if(n.getEdges().size()>0) {
			String st=""; for(Edge e : n.getEdges()) st+=" "+e.getId();
			LOGGER.error("Error when removing node "+n.getId()+". Edges are still linked to it (nb="+n.getEdges().size()+")"+st+". Position="+n.getC());
		}
		if(n.getFaces().size()>0) {
			String st=""; for(Face f : n.getFaces()) st+=" "+f.getId();
			LOGGER.error("Error when removing node "+n.getId()+". Faces are still linked to it (nb="+n.getFaces().size()+")"+st+". Position="+n.getC());
		}
	}

	//Remove an edge from a graph. The edge is supposed not to be linked to any face.
	public void remove(Edge e) {
		boolean b;
		b = edges.remove(e);
		if(!b) LOGGER.error("Error when removing edge "+e.getId()+". Not in graph edges list. Position="+e.getC());

		b = e.getN1().getOutEdges().remove(e);
		if(!b) LOGGER.error("Error when removing edge "+e.getId()+". Not in N1 out edges. Position="+e.getN1().getC());
		b = e.getN2().getInEdges().remove(e);
		if(!b) LOGGER.error("Error when removing edge "+e.getId()+". Not in N2 in edges. Position="+e.getN2().getC());

		b = removeFromSpatialIndex(e);
		if(!b) LOGGER.error("Error when removing edge "+e.getId()+". Not in spatial index. Position="+e.getC());

		if(e.f1 != null) LOGGER.error("Error when removing edge "+e.getId()+". It is still linked to face "+e.f1+". Position="+e.getC());
		if(e.f2 != null) LOGGER.error("Error when removing edge "+e.getId()+". It is still linked to face "+e.f2+". Position="+e.getC());
	}
	public void removeAll(Collection<Edge> es) { for(Edge e:es) remove(e); }


	public void remove(Face f) {
		boolean b;

		//remove face from list
		b = getFaces().remove(f);
		if(!b) LOGGER.error("Could not remove face "+f.getId()+" from graph");

		//break link with edges
		for(Edge e : f.getEdges()){
			if(e.f1==f) e.f1=null;
			else if(e.f2==f) e.f2=null;
			else LOGGER.error("Could not remove link between face "+f.getId()+" and edge "+e.getId()+". Edge was not linked to the face.");
		}
		f.getEdges().clear();

		f.updateGeometry();
	}


	//support for spatial queries

	//nodes
	private Quadtree spIndNode = new Quadtree();
	protected void insertInSpatialIndex(Node n){ spIndNode.insert(new Envelope(n.getC()), n); }
	protected boolean removeFromSpatialIndex(Node n){ return spIndNode.remove(new Envelope(n.getC()), n); }
	public Collection<Node> getNodesAt(Envelope env) { return spIndNode.query(env); }
	public Node getNodeAt(Coordinate c) {
		for(Node n : getNodesAt(new Envelope(c))) if(c.distance(n.getC()) == 0) return n;
		return null;
	}
	public Node getCreateNodeAt(Coordinate c) {
		Node n = getNodeAt(c);
		if(n!=null) return n;
		return this.buildNode(c);
	}

	//edges
	private Quadtree spIndEdge = new Quadtree();
	protected void insertInSpatialIndex(Edge e){ spIndEdge.insert(e.getGeometry().getEnvelopeInternal(), e); }
	protected boolean removeFromSpatialIndex(Edge e){ return spIndEdge.remove(e.getGeometry().getEnvelopeInternal(), e); }
	public Collection<Edge> getEdgesAt(Envelope env) { return spIndEdge.query(env); }

	//faces
	private Quadtree spIndFace = new Quadtree();
	protected void insertInSpatialIndex(Face f){ spIndFace.insert(f.getGeom().getEnvelopeInternal(), f); }
	protected boolean removeFromSpatialIndex(Face f){ return spIndFace.remove(f.getGeom().getEnvelopeInternal(), f); }
	public Collection<Face> getFacesAt(Envelope env) { return spIndFace.query(env); }




	public Collection<Feature> getFaceFeatures(){ return Face.getFaceFeatures(getFaces()); }
	public Collection<Feature> getEdgeFeatures(){ return Edge.getEdgeFeatures(getEdges()); }
	public Collection<Feature> getNodeFeatures(){ return Node.getNodeFeatures(getNodes()); }




	//aggregate two faces
	public Set<Edge> aggregate(Face targetFace, Face delFace) {
		if(delFace==targetFace){
			LOGGER.error("Error: Cannot aggregate a face with itself.");
			return null;
		}

		//get edges to delete (the ones in common)
		Set<Edge> delEdges = targetFace.getEdgesInCommon(delFace);
		if(delEdges.size()==0){
			LOGGER.error("Could not aggregate face "+delFace.getId()+" with face "+targetFace.getId()+": No edge in common.");
			return delEdges;
		}

		boolean b = true;
		//TODO remove this special case maybe?
		if(delFace.isEnclave()){
			//store nodes, to remove them in the end
			Collection<Node> ns = delFace.getNodes();

			//remove face (making hole)
			remove(delFace);

			//remove hole - remove edges
			b = targetFace.getEdges().removeAll(delEdges);
			if(!b) LOGGER.error("Error when aggregating (enclave) face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in removing edges of absorbed face "+delFace.getId());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; remove(e); }

			//remove remaining nodes
			for(Node n:ns)
				if(n.getEdgeNumber()==0)
					remove(n);
		} else {
			//store nodes concerned
			Set<Node> nodes = new HashSet<Node>();
			for(Edge e : delEdges) { nodes.add(e.getN1()); nodes.add(e.getN2()); }

			//get edges to move from delFace from targetFace
			Set<Edge> moveEdge = new HashSet<Edge>();
			b = moveEdge.addAll(delFace.getEdges());
			b = moveEdge.removeAll(delEdges);
			if(moveEdge.size()+delEdges.size()!=delFace.getEdges().size()) LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": inconsistent sets");

			//remove face, leaving a hole
			remove(delFace);

			//remove hole - remove edges
			b = targetFace.getEdges().removeAll(delEdges);
			if(!b) LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in removing edges of absorbing face "+ targetFace.getId()+". Nb="+delEdges.size());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; remove(e); }

			//link remaining edges from absorbed face to target face
			for(Edge e : moveEdge)
				if(e.f1==null) e.f1 = targetFace;
				else if(e.f2==null) e.f2 = targetFace;
				else LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Edge "+e.getId()+" should be linked to null face but it is not. Linked to: "+e.f1+" and "+e.f2);
			b = targetFace.getEdges().addAll(moveEdge);
			if(!b) LOGGER.error("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in adding new edges to absorbing face "+targetFace.getId());

			//remove single nodes
			for(Node n : nodes)
				if(n.getEdgeNumber()==0)
					remove(n);

			//ensure nodes are reduced, which means they do not have a degree 2
			for(Node n : nodes){
				Edge e = n.ensureReduction();
				if(e != null) delEdges.add(e);
			}
		}

		//force faces geometry update
		targetFace.updateGeometry();
		delFace.updateGeometry();

		return delEdges;
	}




	//merge two edges into a new single one
	public Edge merge(Edge e1, Edge e2) {
		if(e1.isClosed() || e2.isClosed()){
			LOGGER.error("Cannot merge edges if one of them is closed.");
			return null;
		}

		//"closed" case
		if(e1.getN1()==e2.getN1() && e1.getN2()==e2.getN2()) return merge(e1.revert(),e2);
		//handle other cases
		else if(e1.getN1()==e2.getN2() && e1.getN2()!=e2.getN1()) return merge(e2,e1);
		else if(e1.getN1()==e2.getN1()) return merge(e1.revert(),e2);
		else if(e1.getN2()==e2.getN2()) return merge(e1,e2.revert());

		//get nodes
		Node n=e1.getN2(), n2=e2.getN2();

		LOGGER.debug("merge around "+n.getId() +" "+ n.getC());

		//build new edge geometry
		int nb1 = e1.getCoords().length, nb2 = e2.getCoords().length;
		Coordinate[] coords = new Coordinate[nb1+nb2-1];
		for(int i=0; i<nb1; i++) coords[i] = e1.getCoords()[i];
		for(int i=nb1; i<nb1+nb2-1; i++) coords[i] = e2.getCoords()[i-nb1+1];

		//disconnect and remove e2
		if(e2.f1!=null) { e2.f1.getEdges().remove(e2); e2.f1=null; }
		if(e2.f2!=null) { e2.f2.getEdges().remove(e2); e2.f2=null; }
		remove(e2);

		//update e1 with new geometry and new final node
		e1.setGeom(new GeometryFactory().createLineString(coords));
		e1.setN2(n2);

		//remove middle node
		remove(n);

		return e2;
	}




	//both nodes are collapsed to the center of the edge
	public void collapseEdge(Edge e) {
		//get nodes
		Node n1 = e.getN1(), n2 = e.getN2();

		//break link edge/faces
		if(e.f1 != null) { e.f1.getEdges().remove(e); e.f1=null; }
		if(e.f2 != null) { e.f2.getEdges().remove(e); e.f2=null; }

		//remove edge
		remove(e);

		//move n1 to edge center
		n1.moveTo( 0.5*(n1.getC().x+n2.getC().x), 0.5*(n1.getC().y+n2.getC().y) );

		//make n1 origin of all edges starting from node n2
		Set<Edge> es;
		es = new HashSet<Edge>(); es.addAll(n2.getOutEdges());
		for(Edge e_ : es) e_.setN1(n1);

		//make n1 destination of all edges going to n2
		es = new HashSet<Edge>(); es.addAll(n2.getInEdges());
		for(Edge e_ : es) e_.setN2(n1);

		//System.out.println(n2.getOutEdges().size() +"   "+ n2.getInEdges().size());

		//remove n2
		remove(n2);
	}

	//find one edge shorter than a threshold values
	public Edge findTooShortEdge(double d) {
		for(Edge e : getEdges())
			if(e.getGeometry().getLength() < d)
				return e;
		return null;
	}

	//collapse too short edges
	public void collapseTooShortEdges(double d) {
		Edge e = findTooShortEdge(d);
		while(e != null) {
			collapseEdge(e);
			e = findTooShortEdge(d);
		}
	}



	//remove edges with similar geometries (based on haussdorff distance)
	//the edges are supposed not to be linked to any face.
	public void removeSimilarDuplicateEdges(double haussdorffDistance) {
		Edge e = findSimilarDuplicateEdgeToRemove(haussdorffDistance);
		while(e != null) {
			remove(e);
			e = findSimilarDuplicateEdgeToRemove(haussdorffDistance);
		}
	}

	private Edge findSimilarDuplicateEdgeToRemove(double haussdorffDistance) {
		for(Edge e : getEdges()) {
			for(Edge e_ : e.getN1().getOutEdges())
				if(e!=e_ && e_.getN2() == e.getN2() && new HausdorffDistance(e.getGeometry(),e_.getGeometry()).getDistance()<haussdorffDistance)
					return getLongest(e,e_);
			for(Edge e_ : e.getN2().getOutEdges())
				if(e!=e_ && e_.getN2() == e.getN1() && new HausdorffDistance(e.getGeometry(),e_.getGeometry()).getDistance()<haussdorffDistance)
					return getLongest(e,e_);
		}
		return null;
	}

	private static Edge getLongest(Edge e1, Edge e2) {
		double d1 = e1.getGeometry().getLength();
		double d2 = e2.getGeometry().getLength();
		if(d1<d2) return e2; else return e1;
	}




	//retrieve graph elements by id
	public Node getNode(String id){ return (Node)getElt(id, getNodes()); }
	public Edge getEdge(String id){ return (Edge)getElt(id, getEdges()); }
	public Face getFace(String id){ return (Face)getElt(id, getFaces()); }
	private GraphElement getElt(String id, Set<? extends GraphElement> elts){
		for(GraphElement ge:elts) if(ge.getId().equals(id)) return ge;
		return null;
	}


	//find edge linking two nodes, if it exists
	public Edge getEdge(Node n1, Node n2) {
		Envelope env = new Envelope(n1.getC(), n2.getC()); env.expandBy(0.1, 0.1);
		for(Edge e : getEdgesAt(env))
			if(e.getN1()==n1 && e.getN2()==n2)
				return e;
		return null;
	}


	public void clear() {
		for(Node n : getNodes()) { n.getInEdges().clear(); n.getOutEdges().clear(); }
		getNodes().clear();
		for(Edge e : getEdges()) e.clear();
		getEdges().clear();
		for(Face f : getFaces()) f.clear();
		getFaces().clear();
	}

	public Collection<LineString> getEdgeGeometries() {
		Collection<LineString> out = new HashSet<>();
		for(Edge e : getEdges()) out.add(e.getGeometry());
		return out;
	}

	/**
	 * Check if two edges are connected. If so, return the connection node.
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	public Node areConnected(Edge e1, Edge e2) {
		if(e1.getN1() == e2.getN1()) return e1.getN1();
		if(e1.getN1() == e2.getN2()) return e1.getN1();
		if(e1.getN2() == e2.getN1()) return e1.getN2();
		if(e1.getN2() == e2.getN2()) return e1.getN2();
		return null;
	}

}
