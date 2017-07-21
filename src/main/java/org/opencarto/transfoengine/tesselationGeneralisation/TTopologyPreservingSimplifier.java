/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * @author julien Gaffuri
 *
 */
public class TTopologyPreservingSimplifier extends Transformation {

	private double resolution;

	public TTopologyPreservingSimplifier(double resolution){
		this.resolution = resolution;
	}

	@Override
	public void apply(Agent agent) {
		Edge e = (Edge) agent.getObject();
		LineString lsIni = e.getGeometry();

		TopologyPreservingSimplifier tr = new TopologyPreservingSimplifier(lsIni);
		tr.setDistanceTolerance(resolution);
		LineString lsFin = (LineString) tr.getResultGeometry();

		e.setGeom(lsFin);
	}

}
