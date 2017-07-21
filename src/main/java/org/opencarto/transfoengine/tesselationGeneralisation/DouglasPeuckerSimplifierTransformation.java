/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;

/**
 * @author julien Gaffuri
 *
 */
public class DouglasPeuckerSimplifierTransformation extends Transformation {

	private double resolution;

	public DouglasPeuckerSimplifierTransformation(double resolution){
		this.resolution = resolution;
	}

	@Override
	public void apply(Agent agent) {
		Edge e = (Edge) agent.getObject();
		LineString lsIni = e.getGeometry();

		//LineString lsFin = (LineString) DouglasPeuckerSimplifier.simplify(lsIni, resolution);
		DouglasPeuckerSimplifier dps = new DouglasPeuckerSimplifier(lsIni);
		dps.setDistanceTolerance(resolution);
		dps.setEnsureValid(true);
		LineString lsFin = (LineString) dps.getResultGeometry();

		e.setGeom(lsFin);
	}

}
