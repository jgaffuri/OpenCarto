package org.opencarto.datamodel.graph;

import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Node<T> {
	Node(){}

	public Coordinate c;
	public T obj;
	public double value;

	private Collection<Edge<T>> inEdges = new ArrayList<Edge<T>>();
	public Collection<Edge<T>> getInEdges() { return inEdges; }

	private Collection<Edge<T>> outEdges = new ArrayList<Edge<T>>();
	public Collection<Edge<T>> getOutEdges() { return outEdges; }

	public Point getGeometry(){
		return new GeometryFactory().createPoint(c);
	}

	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.props.put("VALUE", value);
		return f;
	}
}
