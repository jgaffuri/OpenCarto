package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Node<T> {
	Node(){}

	//the position of the node
	public Coordinate c;

	//an object linked to the node
	public T obj;
	
	//a value linked to the node
	//TODO dictionary instead?
	public double value;

	//the edges, incoming and outgoing
	private Collection<Edge<T>> inEdges = new HashSet<Edge<T>>();
	public Collection<Edge<T>> getInEdges() { return inEdges; }
	private Collection<Edge<T>> outEdges = new HashSet<Edge<T>>();
	public Collection<Edge<T>> getOutEdges() { return outEdges; }

	
	
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
