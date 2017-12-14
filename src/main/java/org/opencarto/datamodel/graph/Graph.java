package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.index.quadtree.Quadtree;

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
	public Edge buildEdge(Node n1, Node n2){ return buildEdge(n1,n2,null); }
	public Edge buildEdge(Node n1, Node n2, Coordinate[] coords){
		Edge e = new Edge(this, n1,n2,coords);
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
		if(!b) LOGGER.error("Error when removing node "+n.getId()+". Not in graph nodes list.");

		b = removeFromSpatialIndex(n);
		if(!b) LOGGER.error("Error when removing node "+n.getId()+". Not in spatial index.");

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
		if(!b) {
			LOGGER.error("Error when removing edge "+e.getId()+". Not in graph edges list. Position="+e.getC());
			LOGGER.error("Force exit"); System.exit(0);
		}

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
	public Node getNodeAt(Coordinate c) {
		for(Node n : (Collection<Node>)spIndNode.query(new Envelope(c))) if(c.distance(n.getC()) == 0) return n;
		return null;
	}

	//edges
	private Quadtree spIndEdge = new Quadtree();
	protected void insertInSpatialIndex(Edge e){ spIndEdge.insert(e.getGeometry().getEnvelopeInternal(), e); }
	protected boolean removeFromSpatialIndex(Edge e){ return spIndEdge.remove(e.getGeometry().getEnvelopeInternal(), e); }
	public Collection<Edge> getEdgesAt(Envelope env) { return spIndEdge.query(env); }

	//faces
	private Quadtree spIndFace = new Quadtree();
	protected void insertInSpatialIndex(Face f){ spIndFace.insert(f.getGeometry().getEnvelopeInternal(), f); }
	protected boolean removeFromSpatialIndex(Face f){ return spIndFace.remove(f.getGeometry().getEnvelopeInternal(), f); }
	public Collection<Face> getFacesAt(Envelope env) { return spIndFace.query(env); }



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
				if(e==null) continue;
				delEdges.add(e);
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
		if(e1.getN1()==e2.getN1() && e1.getN2()==e2.getN2()) merge(e1.revert(),e2);
		//handle other cases
		else if(e1.getN1()==e2.getN2() && e1.getN2()!=e2.getN1()) return merge(e2,e1);
		else if(e1.getN1()==e2.getN1()) return merge(e1.revert(),e2);
		else if(e1.getN2()==e2.getN2()) return merge(e1,e2.revert());

		//get nodes
		Node n=e1.getN2(), n2=e2.getN2();

		LOGGER.debug("merge around "+n.getC());

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


	//retrieve graph elements by id
	public Node getNode(String id){ return (Node)getElt(id, getNodes()); }
	public Edge getEdge(String id){ return (Edge)getElt(id, getEdges()); }
	public Face getFace(String id){ return (Face)getElt(id, getFaces()); }
	private GraphElement getElt(String id, Set<? extends GraphElement> elts){
		for(GraphElement ge:elts) if(ge.getId().equals(id)) return ge;
		return null;
	}


}
