package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.opencarto.algo.base.Scaling;
import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
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
 */
public class Edge extends GraphElement{
	private final static Logger LOGGER = Logger.getLogger(Edge.class.getName());

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

		//add to spatial index
		graph.insertInSpatialIndex(this);
	}

	//the nodes
	private Node n1;
	public Node getN1() { return n1; }
	public void setN1(Node n) {
		if(n==n1) return;
		boolean samePosition = n1.getC().distance(n.getC()) == 0;
		boolean b;
		if(!samePosition){
			b = getGraph().removeFromSpatialIndex(this);
			if(!b) LOGGER.error("Error when changing node 1 of edge "+getId()+". Could not remove it from spatial index.");
		}
		b = n1.getOutEdges().remove(this);   if(!b) LOGGER.error("Error (1) when changing node of edge "+getId());
		n1=n;
		b = n1.getOutEdges().add(this);   if(!b) LOGGER.error("Error (2) when changing node of edge "+getId());
		coords[0]=n.getC();
		if(!samePosition) {
			getGraph().insertInSpatialIndex(this);
			if(f1!=null) f1.updateGeometry();
			if(f2!=null) f2.updateGeometry();
		}
	}
	private Node n2;
	public Node getN2() { return n2; }
	public void setN2(Node n) {
		if(n==n2) return;
		boolean samePosition = n2.getC().distance(n.getC()) == 0;
		boolean b;
		if(!samePosition){
			b = getGraph().removeFromSpatialIndex(this);
			if(!b) LOGGER.error("Error when changing node 1 of edge "+getId()+". Could not remove it from spatial index.");
		}
		b = n2.getInEdges().remove(this);   if(!b) LOGGER.error("Error (1) when changing node of edge "+getId());
		n2=n;
		b = n2.getInEdges().add(this);   if(!b) LOGGER.error("Error (2) when changing node of edge "+getId());
		coords[coords.length-1]=n.getC();
		if(!samePosition) {
			getGraph().insertInSpatialIndex(this);
			if(f1!=null) f1.updateGeometry();
			if(f2!=null) f2.updateGeometry();
		}
	}

	//the geometry
	private Coordinate[] coords;
	public Coordinate[] getCoords() { return coords; }
	public void setGeom(LineString ls) {
		boolean b;
		b = getGraph().removeFromSpatialIndex(this);
		if(!b) LOGGER.error("Error when changing geometry of edge "+getId()+". Could not remove it from spatial index.");
		coords = ls.getCoordinates();
		coords[0] = getN1().getC();
		coords[coords.length-1] = getN2().getC();
		getGraph().insertInSpatialIndex(this);
		if(f1!=null) f1.updateGeometry(); if(f2!=null) f2.updateGeometry();
	}
	public LineString getGeometry(){
		return new GeometryFactory().createLineString(coords);
	}
	public Coordinate getC() { return getGeometry().getCentroid().getCoordinate(); }

	//the faces
	public Face f1=null, f2=null;
	public Collection<Face> getFaces() {
		HashSet<Face> fs = new HashSet<Face>();
		if(f1!=null) fs.add(f1);
		if(f2!=null) fs.add(f2);
		return fs;
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


	//scale the edge.
	public void scale(double factor) { scale(factor, getGeometry().getCentroid().getCoordinate()); }
	public void scale(double factor, Coordinate center) {
		if(factor == 1) return;

		//remove edge from spatial index
		boolean b = getGraph().removeFromSpatialIndex(this);
		if(!b) LOGGER.warn("Could not remove edge from spatial index when scaling face");

		//scale edges' internal coordinates
		for(Coordinate c : getCoords()){
			if(c==getN1().getC()) continue;
			if(c==getN2().getC()) continue;
			Scaling.apply(c,center,factor);
		}

		//scale nodes
		Scaling.apply(getN1().getC(),center,factor);
		if(!isClosed())
			Scaling.apply(getN2().getC(),center,factor);

		//update spatial index
		getGraph().insertInSpatialIndex(this);

		//force face geometry update
		for(Face f : getFaces()) f.updateGeometry();
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.id=getId();
		f.set("id", getId());
		f.set("value", value);
		f.set("n1", n1.getId());
		f.set("n2", n2.getId());
		f.set("face_1", f1!=null?f1.getId():null);
		f.set("face_2", f2!=null?f2.getId():null);
		f.set("coastal", getCoastalType());
		f.set("topo", getTopologicalType());
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
		else LOGGER.error("Could not break link between edge "+this.getId()+" and face "+face.getId());
		face.updateGeometry();
	}

	//check edge is ok, that is:
	// - it does not self intersects (it is "simple")
	// - it does not intersects another edge
	public boolean isOK(boolean checkIsSimple, boolean checkEdgeToEdgeIntersection) {
		LineString g = getGeometry();

		if(g==null) return false;
		if(g.isEmpty()) return false;
		//if(!g.isValid()) return false; //unnecessary, since it is also tested in isSimple() method
		if(checkIsSimple && !g.isSimple()) return false;

		if(checkEdgeToEdgeIntersection){
			//check face does not overlap other edges
			Envelope env = g.getEnvelopeInternal();
			for(Edge e_ : (Collection<Edge>)getGraph().getEdgesAt(env)){
				if(this==e_) continue;
				LineString g2 = e_.getGeometry();

				if(g2==null || g2.isEmpty()) {
					LOGGER.warn("Null/empty geometry found for edge "+e_.getId());
					continue;
				}
				if(!g2.getEnvelopeInternal().intersects(env)) continue;

				try {
					//improve speed by using right geometrical predicate. crosses? overlap?
					//if(!g2.intersects(g)) continue;
					//if(g2.touches(g)) continue;
					//if(!g2.overlaps(g)) continue;

					//analyse intersection
					Geometry inter = g.intersection(g2);
					if(inter.isEmpty()) continue;
					if(inter.getLength()>0)
						return false;
					for(Coordinate c : inter.getCoordinates()){
						if( c.distance(getN1().getC())==0 || c.distance(getN2().getC())==0 ) continue;
						return false;
					}

					return false;
				} catch (Exception e){ return false; }
			}
		}

		return true;
	}

	public Edge clear() {
		this.n1 = null;
		this.n2 = null;
		this.coords = null;
		this.f1 = null;
		this.f2 = null;
		return this;
	}
}
