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
	public Domain d1=null, d2=null;
	public Collection<Domain> getDomains() {
		HashSet<Domain> ds = new HashSet<Domain>();
		if(d1!=null) ds.add(d1);
		if(d2!=null) ds.add(d2);
		return ds;
	}
	/*private Domain dL;
	public Domain getDomainLeft() { return dL; }
	private Domain dR;
	public Domain getDomainRight() { return dR; }*/


	//an object linked to the edge
	public Object obj;
	//a value linked to the edge
	public double value;



	public boolean isIsthmus(){ return d1==null && d2==null; }
	public boolean isCoastal(){ return d1==null ^ d2==null; }
	public String getCoastalType() {
		if(isIsthmus()) return "isthmus";
		if(isCoastal()) return "coastal";
		return "non_coastal";
	}

	public boolean isDangle(){ return n1.getEdges().size()==1 ^ n2.getEdges().size()==1; }
	public boolean isIsolated(){ return n1.getEdges().size()==1 || n2.getEdges().size()==1; }
	public String getTopologicalType() {
		if(isDangle()) return "dangle";
		if(isIsolated()) return "isolated";
		return "normal";
	}


	//build the geometry
	public LineString getGeometry(){
		return new GeometryFactory().createLineString(coords);
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.id=id;
		f.getProperties().put("id", id);
		f.getProperties().put("value", value);
		f.getProperties().put("n1", n1.getId());
		f.getProperties().put("n2", n2.getId());
		f.getProperties().put("domain_1", d1);
		f.getProperties().put("domain_2", d2);
		f.getProperties().put("coastal", getCoastalType());
		f.getProperties().put("topo", getTopologicalType());
		return f;
	}
}
