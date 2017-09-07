package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
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
		for(Edge e : edges) if(e.f1==null) e.f1=f; else e.f2=f;
		faces.add(f);
		return f;
	}



	//Remove a node from the graph. The node is supposed not to be linked to any edge.
	public void remove(Node n) {
		boolean b;
		b = nodes.remove(n);
		if(!b) LOGGER.severe("Error when removing node "+n.getId()+". Not in graph nodes list.");

		b = spIndNode.remove(new Envelope(n.getC()), n);
		if(!b) LOGGER.severe("Error when removing node "+n.getId()+". Not in spatial index.");

		if(n.getEdges().size()>0) LOGGER.severe("Error when removing node "+n.getId()+". Edges are still linked to it (nb="+n.getEdges().size()+")");
		if(n.getFaces().size()>0) LOGGER.severe("Error when removing node "+n.getId()+". Faces are still linked to it (nb="+n.getFaces().size()+")");
	}

	//Remove an edge from a graph. The edge is supposed not to be linked to any face.
	public void remove(Edge e) {
		boolean b;
		b = edges.remove(e);
		if(!b) LOGGER.severe("Error when removing edge "+e.getId()+". Not in graph edges list.");

		b = e.getN1().getOutEdges().remove(e);
		if(!b) LOGGER.severe("Error when removing edge "+e.getId()+". Not in N1 out edges");
		b = e.getN2().getInEdges().remove(e);
		if(!b) LOGGER.severe("Error when removing edge "+e.getId()+". Not in N2 in edges");

		b = spIndEdge.remove(e.getGeometry().getEnvelopeInternal(), e);
		if(!b) LOGGER.severe("Error when removing edge "+e.getId()+". Not in spatial index.");

		if(e.f1 != null) LOGGER.severe("Error when removing edge "+e.getId()+". It is still linked to face "+e.f1);
		if(e.f2 != null) LOGGER.severe("Error when removing edge "+e.getId()+". It is still linked to face "+e.f2);
	}
	public void removeAll(Collection<Edge> es) { for(Edge e:es) remove(e); }


	public void remove(Face f) {
		boolean b;

		//remove face from list
		b = getFaces().remove(f);
		if(!b) LOGGER.severe("Could not remove face "+f.getId()+" from graph");

		//break link with edges
		for(Edge e : f.getEdges()){
			if(e.f1==f) e.f1=null;
			else if(e.f2==f) e.f2=null;
			else LOGGER.severe("Could not remove link between face "+f.getId()+" and edge "+e.getId());
		}

		//unnecessary
		//f.getEdges().clear();

		//update spatial index
		b = spIndFace.remove(f.getGeometry().getEnvelopeInternal(), f);
		if(!b) LOGGER.severe("Error when removing face "+f.getId()+". Not in spatial index.");

		f.geomUpdateNeeded = true;
	}


	//support for spatial queries
	private SpatialIndex spIndNode = new Quadtree();
	public SpatialIndex getSpatialIndexNode() { return spIndNode; }
	private SpatialIndex spIndEdge = new Quadtree();
	public SpatialIndex getSpatialIndexEdge() { return spIndEdge; }
	private SpatialIndex spIndFace = new Quadtree();
	public SpatialIndex getSpatialIndexFace() { return spIndFace; }

	public Node getNodeAt(Coordinate c) {
		Envelope env = new Envelope(c);
		List<?> elts = spIndNode.query(env);
		for(Object elt : elts){
			Node n = (Node)elt;
			if(c.distance(n.getC()) == 0) return n;
		}
		return null;
	}

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
	public Set<Edge> aggregate(Face targetFace, Face delFace) {
		if(delFace==targetFace){
			LOGGER.severe("Error: Cannot aggregate a face with itself.");
			return null;
		}

		//get edges to delete (the ones in common)
		Set<Edge> delEdges = targetFace.getEdgesInCommon(delFace);
		if(delEdges.size()==0){
			LOGGER.severe("Could not aggregate face "+delFace.getId()+" with face "+targetFace.getId()+": No edge in common.");
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
			if(!b) LOGGER.severe("Error when aggregating (enclave) face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in removing edges of absorbed face "+delFace.getId());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; remove(e); }

			//remove remaining nodes
			for(Node n:ns) remove(n);
		} else {
			//store nodes concerned
			Set<Node> nodes = new HashSet<Node>();
			for(Edge e : delEdges) { nodes.add(e.getN1()); nodes.add(e.getN2()); }

			//get edges to move from delFace from targetFace
			Set<Edge> moveEdge = new HashSet<Edge>();
			b = moveEdge.addAll(delFace.getEdges());
			b = moveEdge.removeAll(delEdges);
			if(moveEdge.size()+delEdges.size()!=delFace.getEdges().size()) LOGGER.severe("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": inconsistent sets");

			//remove face, leaving a hole
			remove(delFace);

			//remove hole - remove edges
			b = targetFace.getEdges().removeAll(delEdges);
			if(!b) LOGGER.severe("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in removing edges of absorbing face "+ targetFace.getId()+". Nb="+delEdges.size());
			for(Edge e : delEdges){ e.f1=null; e.f2=null; remove(e); }

			//link remaining edges from absorbed face to target face
			for(Edge e : moveEdge)
				if(e.f1==delFace) e.f1 = targetFace;
				else if(e.f2==delFace) e.f2 = targetFace;
				else LOGGER.severe("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Edge "+e.getId()+" should be linked to deleted face "+delFace.getId()+" but it is not. Linked to: "+e.f1+" and "+e.f2);
			b = targetFace.getEdges().addAll(moveEdge);
			if(!b) LOGGER.severe("Error when aggregating face "+delFace.getId()+" into face "+targetFace.getId()+": Failed in adding new edges to absorbing face "+targetFace.getId());
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

		//force faces geometry update
		targetFace.geomUpdateNeeded = true;
		delFace.geomUpdateNeeded = true;

		return delEdges;
	}




	//merge two edges into a new single one
	public Edge merge(Edge e1, Edge e2) {
		if(e1.isClosed() || e2.isClosed()){
			LOGGER.severe("Cannot merge edges if one of them is closed.");
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

		LOGGER.fine("merge around "+n.getC());

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

}
