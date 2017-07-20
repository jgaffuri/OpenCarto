/**
 * 
 */
package org.opencarto.transfoengine.transformations;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

/**
 * @author julien Gaffuri
 *
 */
public class DPTransormation extends Transformation {

	private double resolution;

	public DPTransormation(double resolution){
		this.resolution = resolution;
	}

	@Override
	public void apply(Agent agent) {
		Edge e = (Edge) agent.getObject();
		LineString lsIni = e.getGeometry();
		LineString lsFin = (LineString) DouglasPeuckerSimplifier.simplify(lsIni, resolution);
		e.setGeom(lsFin);
	}

}
