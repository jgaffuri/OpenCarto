/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

/**
 * A graph domain
 * 
 * @author julien Gaffuri
 *
 */
public class Domain {

	Domain(){}

	//the edges
	//need for edges ring, etc. ?
	private Collection<Edge> edges = new HashSet<Edge>();
	public Collection<Edge> getEdges() { return edges; }


	//an object linked to the edge
	public Object obj;
	//a value linked to the domain
	public double value;




	//build the geometry
	public MultiPolygon getGeometry(){
		Polygonizer pg = new Polygonizer();
		for(Edge e : edges) pg.add(e.getGeometry());
		Collection<Polygon> polys = pg.getPolygons();
		pg = null;

		if(polys.size() == 1) return (MultiPolygon)JTSGeomUtil.toMulti(polys.iterator().next());

		//return polygon whose external ring has the largest area
		double maxArea = -1; Polygon polyMax = null;
		for(Polygon poly : polys){
			double area = poly.getExteriorRing().getArea();
			if(area<maxArea) continue;
			maxArea = area; polyMax = poly;
		}

		return (MultiPolygon)JTSGeomUtil.toMulti(polyMax);
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.getProperties().put("VALUE", value);
		return f;
	}

}
