/**
 * 
 */
package org.opencarto.datamodel.graph;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * @author julien Gaffuri
 *
 */
public class Domain<T> {

	//TODO
	//based on edges
	//need for edges ring, etc.


	//a value linked to the domain
	//TODO dictionary instead?
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
