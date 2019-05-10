package org.opencarto.datamodel.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

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

}
