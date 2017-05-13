/**
 * 
 */
package org.opencarto.datamodel.graph;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * A graph domain
 * 
 * @author julien Gaffuri
 *
 */
public class Domain {

	//TODO
	//based on edges
	//need for edges ring, etc.


	//an object linked to the edge
	public Object obj;
	//a value linked to the domain
	public double value;



	//build the geometry
	public MultiPolygon getGeometry(){
		//TODO
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
