/**
 * 
 */
package org.opencarto.transfoengine;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class State {

	public State(Geometry geom){
		this.geom = geom;
	}
	
	public Geometry geom;
}
