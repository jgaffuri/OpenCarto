package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * A graph node.
 * It is located somewhere and is linked to incoming and outcoming edges.
 * 
 * @author julien gaffuri
 *
 * @param <N>
 * @param <E>
 * @param <D>
 */
public class Node extends GraphElement{
	//private final static Logger LOGGER = Logger.getLogger(Node.class));

	private static int ID = 0;

	Node(Graph graph, Coordinate c){
		super(graph,"N"+(ID++));
		this.c=c;
		graph.getSpatialIndexNode().insert(new Envelope(getC()), this);
	}

	//the position of the node
	private Coordinate c;
	public Coordinate getC() { return c; }

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

	//get edges number
	public int getEdgeNumber() {
		return getInEdges().size() + getOutEdges().size();
	}

	//get list of faces (computed on-the-fly)
	public HashSet<Face> getFaces(){
		HashSet<Face> faces = new HashSet<Face>();
		for(Edge e : getOutEdges()) faces.addAll(e.getFaces());
		//for(Edge e : getInEdges()) faces.add(e.getFaceLeft());
		//for(Edge e : getOutEdges()) faces.add(e.getFaceRight());
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
		f.getProperties().put("id", getId());
		f.getProperties().put("value", value);
		f.getProperties().put("edg_in_nb", getInEdges().size());
		f.getProperties().put("edg_out_nb", getOutEdges().size());
		String txt=null;
		for(Edge e:getInEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.getProperties().put("edges_in", txt);
		txt=null;
		for(Edge e:getOutEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.getProperties().put("edges_out", txt);
		Collection<Face> faces = getFaces();
		f.getProperties().put("face_nb", faces .size());
		txt=null;
		for(Face d:faces) txt=(txt==null?"":txt+";")+d.getId();
		f.getProperties().put("faces", txt);
		f.getProperties().put("type", getType());
		return f;
	}

	public void moveTo(double x, double y) {
		if(getC().distance(new Coordinate(x,y))==0) return;

		getGraph().getSpatialIndexNode().remove(new Envelope(getC()), this);
		getC().x = x;
		getC().y = y;
		getGraph().getSpatialIndexNode().insert(new Envelope(getC()), this);

		for(Face f : getFaces()) f.geomUpdateNeeded();

		//update edges coords
		//for(Edge e:getOutEdges()) e.coords[0]=getC();
		//for(Edge e:getInEdges()) e.coords[e.coords.length-1]=getC();
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
