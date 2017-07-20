/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.measure.Granularity;
import org.opencarto.algo.measure.Granularity.Measurement;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class EdgeGranularity extends Constraint {
	double goalResolution, currentResolution;

	public EdgeGranularity(Agent agent, double goalResolution) {
		super(agent);
		this.goalResolution = goalResolution;
	}



	@Override
	public void computeCurrentValue() {
		LineString g = ((Edge)getAgent().getObject()).getGeometry();
		Measurement m = Granularity.get(g, goalResolution);
		currentResolution = m.average;
	}

	@Override
	public void computeGoalValue() {}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) { satisfaction=10; return; }
		if(currentResolution>=goalResolution) { satisfaction=10; return; }
		satisfaction = 10-10*Math.abs(goalResolution-currentResolution)/goalResolution;
	}

}
