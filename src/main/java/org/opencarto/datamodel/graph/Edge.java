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

	//the nodes
	private Node<T> n1;
	public Node<T> getN1() { return n1; }
	private Node<T> n2;
	public Node<T> getN2() { return n2; }

	//the domains
	private Domain<T> dL;
	public Domain<T> getDomainLeft() { return dL; }
	private Domain<T> dR;
	public Domain<T> getDomainRight() { return dR; }

	//a value linked to the edge
	//TODO dictionary instead?
	public double value;


	
	//build the geometry
	public LineString getGeometry(){
		return new GeometryFactory().createLineString(new Coordinate[]{n1.c, n2.c});
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.getProperties().put("VALUE", value);
		return f;
	}
}
