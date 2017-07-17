/**
 * 
 */
package org.opencarto.datamodel.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * A graph domain
 * 
 * @author julien Gaffuri
 *
 */
public class Domain {
	Domain(Collection<Edge> edges){
		this.edges = edges;
	}

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
		//TODO - tricky!?
		//use linemerger on edges
		return null;
	}

	//build a feature
	public Feature toFeature(){
		Feature f = new Feature();
		f.setGeom(getGeometry());
		f.getProperties().put("VALUE", value);
		return f;
	}

}
