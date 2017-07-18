package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * A graph node
 * 
 * @author julien gaffuri
 *
 * @param <N>
 * @param <E>
 * @param <D>
 */
public class Node {
	private static int ID = 0;

	Node(Coordinate c){
		this.c=c;
		this.id="NODE_"+(ID++);
	}

	//the id
	private String id;
	public String getId(){ return id; }

	//the position of the node
	public Coordinate c;

	//an object linked to the node
	public Object obj;
	//a value linked to the node
	public double value;

	//the edges, incoming and outgoing
	private Collection<Edge> inEdges = new HashSet<Edge>();
	public Collection<Edge> getInEdges() { return inEdges; }
	private Collection<Edge> outEdges = new HashSet<Edge>();
	public Collection<Edge> getOutEdges() { return outEdges; }


	//get list of domains (computed on-the-fly)
	public HashSet<Domain> getDomains(){
		HashSet<Domain> domains = new HashSet<Domain>();
		for(Edge e : getOutEdges()) domains.addAll(e.getDomains());
		//for(Edge e : getInEdges()) domains.add(e.getDomainLeft());
		//for(Edge e : getOutEdges()) domains.add(e.getDomainRight());
		return domains;
	}



	//build a geometry
	public Point getGeometry(){
		return new GeometryFactory().createPoint(c);
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.getProperties().put("VALUE", value);
		return f;
	}
}
