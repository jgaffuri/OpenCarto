package org.opencarto.datamodel.graph;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class Edge<T> {
	Edge(Node<T> n1, Node<T> n2) {
		this.n1=n1;
		this.n2=n2;
		n1.getOutEdges().add(this);
		n2.getInEdges().add(this);
	}

	private Node<T> n1;
	public Node<T> getN1() { return n1; }
	private Node<T> n2;
	public Node<T> getN2() { return n2; }

	public double value;

	public LineString getGeometry(){
		return new GeometryFactory().createLineString(new Coordinate[]{n1.c, n2.c});
	}

	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.props.put("VALUE", value);
		return f;
	}
}
