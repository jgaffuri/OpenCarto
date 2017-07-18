/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * A graph domain
 * 
 * @author julien Gaffuri
 *
 */
public class Domain {
	private static int ID = 0;

	Domain(){
		this.id="D"+(ID++);
	}

	//the id
	private String id;
	public String getId(){ return id; }

	//the edges
	//need for edges ring, etc. ?
	private Collection<Edge> edges = new HashSet<Edge>();
	public Collection<Edge> getEdges() { return edges; }


	//an object linked to the edge
	public Object obj;
	//a value linked to the domain
	public double value;




	//build the geometry
	public Polygon getGeometry(){
		Polygonizer pg = new Polygonizer();
		for(Edge e : edges) pg.add(e.getGeometry());
		Collection<Polygon> polys = pg.getPolygons();
		pg = null;

		//if(polys.size() == 1) return polys.iterator().next();

		//return polygon whose external ring has the largest area
		double maxArea = -1; Polygon maxPoly = null;
		for(Polygon poly : polys){
			double area = poly.getEnvelopeInternal().getArea();
			if(area > maxArea){
				maxArea = area;
				maxPoly = poly;
			}
		}
		return maxPoly;
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.id=id;
		f.getProperties().put("ID", id);
		f.getProperties().put("VALUE", value);
		f.getProperties().put("EDG_NB", getEdges().size());
		String txt=null;
		for(Edge e:getEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.getProperties().put("EDG", txt);
		return f;
	}

}
