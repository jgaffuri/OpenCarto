package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * A graph (directed) edge
 * It is defined by an origin node and a destination node.
 * It can be linked to a maximum of two faces.
 * Its geometry is a LineString.
 * 
 * @author julien gaffuri
 *
 * @param <N>
 * @param <E>
 * @param <D>
 */
public class Edge extends GraphElement{
	public final static Logger LOGGER = Logger.getLogger(Edge.class.getName());

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
	public void setN1(Node n) {
		if(n==n1) return;
		boolean b;
		b = n1.getOutEdges().remove(this);   if(!b) LOGGER.severe("Error (1) when changing node of edge "+getId());
		n1=n;
		b = n1.getOutEdges().add(this);   if(!b) LOGGER.severe("Error (2) when changing node of edge "+getId());
		coords[0]=n.getC();
	}
	private Node n2;
	public Node getN2() { return n2; }
	public void setN2(Node n) {
		if(n==n2) return;
		boolean b;
		b = n2.getInEdges().remove(this);   if(!b) LOGGER.severe("Error (1) when changing node of edge "+getId());
		n2=n;
		b = n2.getInEdges().add(this);   if(!b) LOGGER.severe("Error (2) when changing node of edge "+getId());
		coords[coords.length-1]=n.getC();
	}

	//the geometry
	Coordinate[] coords;
	public Coordinate[] getCoords() { return coords; }
	public void setGeom(LineString ls) {
		getGraph().getSpatialIndexEdge().remove(getGeometry().getEnvelopeInternal(), this);
		coords=ls.getCoordinates();
		coords[0]=getN1().getC();
		coords[coords.length-1]=getN2().getC();
		getGraph().getSpatialIndexEdge().insert(getGeometry().getEnvelopeInternal(), this);
	}
	public LineString getGeometry(){
		return new GeometryFactory().createLineString(coords);
	}


	//the faces
	public Face f1=null, f2=null;
	public Collection<Face> getFaces() {
		HashSet<Face> fs = new HashSet<Face>();
		if(f1!=null) fs.add(f1);
		if(f2!=null) fs.add(f2);
		return fs;
	}
	/*private Face fL;
	public Face getFaceLeft() { return fL; }
	private Face fR;
	public Face getFaceRight() { return fR; }*/



	//reverse the edge
	public Edge revert() {
		//revert geometry
		Coordinate[] cs = coords;
		coords = new Coordinate[cs.length];
		for(int i=0;i<cs.length;i++) coords[i]=cs[cs.length-1-i];
		cs = null;
		//Revert nodes
		/*boolean b;
		b = n1.getOutEdges().remove(this);
		if(!b) LOGGER.severe("Error (1) in revert of "+getId());
		b = n1.getInEdges().add(this);
		if(!b) LOGGER.severe("Error (2) in revert of "+getId());
		b = n2.getInEdges().remove(this);
		if(!b) LOGGER.severe("Error (3) in revert of "+getId());
		b = n2.getOutEdges().add(this);
		if(!b) LOGGER.severe("Error (4) in revert of "+getId());*/
		Node n=getN1(); setN1(getN2()); setN2(n);
		return this;
	}


	public boolean isIsthmus(){ return f1==null && f2==null; }
	public boolean isCoastal(){ return f1==null || f2==null; }
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
		f.id=getId();
		f.getProperties().put("id", getId());
		f.getProperties().put("value", value);
		f.getProperties().put("n1", n1.getId());
		f.getProperties().put("n2", n2.getId());
		f.getProperties().put("face_1", f1!=null?f1.getId():null);
		f.getProperties().put("face_2", f2!=null?f2.getId():null);
		f.getProperties().put("coastal", getCoastalType());
		f.getProperties().put("topo", getTopologicalType());
		return f;
	}

	//for closed edges
	public double getArea() {
		if(!isClosed()) return -1;
		return new GeometryFactory().createPolygon(coords).getArea();
	}

	public void breakLinkWithFace(Face face) {
		if(f1==face) { f1=null; face.getEdges().remove(this); }
		else if(f2==face) { f2=null; face.getEdges().remove(this); }
		else LOGGER.severe("Could not break link between edge "+this.getId()+" and face "+face.getId());
	}

}
