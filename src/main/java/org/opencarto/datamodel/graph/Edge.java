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
public class Edge extends GraphElement{
	private static int ID = 0;

	Edge(Graph graph, Node n1, Node n2) { this(graph,n1,n2,new Coordinate[]{n1.getC(), n2.getC()}); }
	Edge(Graph graph, Node n1, Node n2, Coordinate[] coords) {
		super(graph,"E"+(ID++));
		this.n1=n1;
		this.n2=n2;
		n1.getOutEdges().add(this);
		n2.getInEdges().add(this);
		this.coords = coords;
		//ensures initial and final coordinates are the ones of the nodes
		coords[0]=getN1().getC();
		coords[coords.length-1]=getN2().getC();
	}

	//the nodes
	private Node n1;
	public Node getN1() { return n1; }
	private Node n2;
	public Node getN2() { return n2; }

	//the geometry
	private Coordinate[] coords;
	public Coordinate[] getCoords() { return coords; }
	public void setGeom(LineString ls) {
		graph.getSpatialIndexEdge().remove(getGeometry().getEnvelopeInternal(), this);
		coords=ls.getCoordinates();
		coords[0]=getN1().getC();
		coords[coords.length-1]=getN2().getC();
		graph.getSpatialIndexEdge().insert(getGeometry().getEnvelopeInternal(), this);
	}
	public LineString getGeometry(){
		return new GeometryFactory().createLineString(coords);
	}


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


	public boolean isIsthmus(){ return d1==null && d2==null; }
	public boolean isCoastal(){ return d1==null || d2==null; }
	public String getCoastalType() {
		if(isIsthmus()) return "isthmus";
		if(isCoastal()) return "coastal";
		return "non_coastal";
	}

	public boolean isDangle(){ return n1!=n2 && n1.getEdges().size()==1 ^ n2.getEdges().size()==1; }
	public boolean isIsolated(){ return n1!=n2 && n1.getEdges().size()==1 && n2.getEdges().size()==1; }
	public boolean isClosed(){ return n1==n2; }
	public String getTopologicalType() {
		if(isDangle()) return "dangle";
		if(isIsolated()) return "isolated";
		if(isClosed()) return "closed";
		return "normal";
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
		f.getProperties().put("domain_1", d1!=null?d1.getId():null);
		f.getProperties().put("domain_2", d2!=null?d2.getId():null);
		f.getProperties().put("coastal", getCoastalType());
		f.getProperties().put("topo", getTopologicalType());
		return f;
	}
}
