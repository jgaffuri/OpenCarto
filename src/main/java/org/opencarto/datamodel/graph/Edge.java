package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * A graph (directed) edge
 * 
 * @author julien gaffuri
 *
 * @param <N>
 * @param <E>
 * @param <D>
 */
public class Edge {

	Edge(Node n1, Node n2) { this(n1,n2,new Coordinate[]{n1.c, n2.c}); }
	Edge(Node n1, Node n2, Coordinate[] coords) {
		this.n1=n1;
		this.n2=n2;
		n1.getOutEdges().add(this);
		n2.getInEdges().add(this);
		this.coords = coords; //TODO check initial and final coordinates are the ones of the nodes?
	}

	//the nodes
	private Node n1;
	public Node getN1() { return n1; }
	private Node n2;
	public Node getN2() { return n2; }

	//the geometry
	private Coordinate[] coords;

	//the domains
	public Collection<Domain> domains = new HashSet<Domain>();
	/*private Domain dL;
	public Domain getDomainLeft() { return dL; }
	private Domain dR;
	public Domain getDomainRight() { return dR; }*/


	//an object linked to the edge
	public Object obj;
	//a value linked to the edge
	public double value;






	//build the geometry
	public LineString getGeometry(){
		return new GeometryFactory().createLineString(coords);
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.getProperties().put("VALUE", value);
		return f;
	}
}
