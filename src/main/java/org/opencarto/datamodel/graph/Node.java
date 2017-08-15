package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
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
	private static int ID = 0;

	Node(Graph graph, Coordinate c){
		super(graph,"N"+(ID++));
		this.c=c;
	}

	//the position of the node
	private Coordinate c;
	public Coordinate getC() { return c; }
	public void setC(double x, double y) { c.x=x; c.y=y; }

	//the edges, incoming and outgoing
	private Collection<Edge> inEdges = new HashSet<Edge>();
	public Collection<Edge> getInEdges() { return inEdges; }
	private Collection<Edge> outEdges = new HashSet<Edge>();
	public Collection<Edge> getOutEdges() { return outEdges; }
	public Collection<Edge> getEdges() {
		HashSet<Edge> out = new HashSet<Edge>();
		out.addAll(inEdges); out.addAll(outEdges);
		return out;
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

}
