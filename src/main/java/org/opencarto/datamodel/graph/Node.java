package org.opencarto.datamodel.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opencarto.datamodel.Feature;

/**
 * A graph node.
 * It is located somewhere and is linked to incoming and outcoming edges.
 * 
 * @author julien gaffuri
 *
 */
public class Node extends GraphElement{
	//private final static Logger LOGGER = Logger.getLogger(Node.class.getName()));

	private static int ID = 0;

	Node(Graph graph, Coordinate c){
		super(graph,"N"+(ID++));
		this.c=c;
		graph.insertInSpatialIndex(this);
	}

	//the position of the node
	private Coordinate c;
	public Coordinate getC() { return c; }

	public void moveTo(double x, double y) {
		if(getC().distance(new Coordinate(x,y))==0) return;

		//move position, updating the spatial index
		getGraph().removeFromSpatialIndex(this);
		getC().x = x;
		getC().y = y;
		getGraph().insertInSpatialIndex(this);

		//update faces geometries
		for(Face f : getFaces()) f.updateGeometry();

		//update edges coords
		//for(Edge e:getOutEdges()) e.coords[0]=getC();
		//for(Edge e:getInEdges()) e.coords[e.coords.length-1]=getC();
	}

	//the edges, incoming and outgoing
	private Set<Edge> inEdges = new HashSet<Edge>();
	public Set<Edge> getInEdges() { return inEdges; }
	private Set<Edge> outEdges = new HashSet<Edge>();
	public Set<Edge> getOutEdges() { return outEdges; }
	public Set<Edge> getEdges() {
		Set<Edge> out = new HashSet<Edge>();
		out.addAll(inEdges); out.addAll(outEdges);
		return out;
	}
	public ArrayList<Edge> getEdgesAsList() {
		ArrayList<Edge> out = new ArrayList<Edge>();
		out.addAll(inEdges); out.addAll(outEdges);
		return out;
	}

	//get edges number
	public int getEdgeNumber() {
		return getInEdges().size() + getOutEdges().size();
	}

	//get list of faces (computed on-the-fly)
	public HashSet<Face> getFaces(){
		HashSet<Face> faces = new HashSet<Face>();
		for(Edge e : getOutEdges()) faces.addAll(e.getFaces());
		return faces;
	}



	public boolean isDangle(){ return getInEdges().size()+getOutEdges().size()==1; }
	//TODO is "isthmus"/junction ?
	private boolean isFictious(){ return getEdges().size()==1 && getInEdges().size()+getOutEdges().size()==2; }
	public boolean isEnclave(){ return isFictious() && !isCoastal(); }
	public boolean isIsland(){ return isFictious() && isCoastal(); }
	public boolean isCoastal(){
		for(Edge e:getEdges()) if(e.isCoastal()) return true;
		return false;
	}
	public String getType() {
		if(isDangle()) return "dangle";
		if(isEnclave()) return "enclave";
		if(isIsland()) return "island";
		if(isCoastal()) return "coastal";
		return "normal";
	}



	//build a geometry
	public Point getGeometry(){
		return new GeometryFactory().createPoint(c);
	}

	//build feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.id=getId();
		f.set("id", getId());
		f.set("value", value);
		f.set("edg_in_nb", getInEdges().size());
		f.set("edg_out_nb", getOutEdges().size());
		String txt=null;
		for(Edge e:getInEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.set("edges_in", txt);
		txt=null;
		for(Edge e:getOutEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.set("edges_out", txt);
		Collection<Face> faces = getFaces();
		f.set("face_nb", faces .size());
		txt=null;
		for(Face d:faces) txt=(txt==null?"":txt+";")+d.getId();
		f.set("faces", txt);
		f.set("type", getType());
		return f;
	}

	public static Collection<Feature> getNodeFeatures(Collection<Node> ns){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(Node n:ns)
			fs.add(n.toFeature());
		return fs;		
	}

	//ensure a node degree is not 2. If it is, merge the two edges.
	//returns the deleted edge
	public Edge ensureReduction() {
		Collection<Edge> es = getEdges();
		if(es.size()!=2) return null;
		Iterator<Edge> it = es.iterator();
		Edge e1=it.next(), e2=it.next();
		if(e1.isClosed() || e2.isClosed()) return null;
		return getGraph().merge(e1,e2);
	}

}
