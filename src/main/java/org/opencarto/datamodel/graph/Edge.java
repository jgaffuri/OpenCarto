package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
	private static int ID = 0;

	Edge(Node n1, Node n2) { this(n1,n2,new Coordinate[]{n1.c, n2.c}); }
	Edge(Node n1, Node n2, Coordinate[] coords) {
		this.id="E"+(ID++);
		this.n1=n1;
		this.n2=n2;
		n1.getOutEdges().add(this);
		n2.getInEdges().add(this);
		this.coords = coords; //TODO check initial and final coordinates are the ones of the nodes?
	}

	//the id
	private String id;
	public String getId(){ return id; }

	//the nodes
	private Node n1;
	public Node getN1() { return n1; }
	private Node n2;
	public Node getN2() { return n2; }

	//the geometry
	public Coordinate[] coords;

	//the domains
	private Collection<Domain> domains = new HashSet<Domain>();
	public Collection<Domain> getDomains() { return domains; }
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
		f.id=id;
		f.getProperties().put("VALUE", value);
		f.getProperties().put("N1", n1.getId());
		f.getProperties().put("N2", n2.getId());
		int nbD = getDomains().size();
		if(nbD>2) System.err.println("Edge with more than 2 domains!? nb="+nbD+"  "+getGeometry());
		Iterator<Domain> it = getDomains().iterator();
		f.getProperties().put("DOM_1", nbD>=1?it.next():null);
		f.getProperties().put("DOM_2", nbD>=2?it.next():null);
		return f;
	}
}
