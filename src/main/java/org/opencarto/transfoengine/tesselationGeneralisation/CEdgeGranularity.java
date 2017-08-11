/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.algo.measure.Granularity;
import org.opencarto.algo.measure.Granularity.Measurement;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class CEdgeGranularity extends Constraint {
	double goalResolution, currentResolution;

	public CEdgeGranularity(Agent agent, double goalResolution) {
		super(agent);
		this.goalResolution = goalResolution;
	}

	@Override
	public void computeCurrentValue() {
		LineString g = ((Edge)getAgent().getObject()).getGeometry();
		Measurement m = Granularity.get(g, goalResolution);
		if(Double.isNaN(m.averageBelow)) currentResolution = m.average;
		else currentResolution = m.averageBelow;
	}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) { satisfaction=10; return; }
		if(currentResolution>=goalResolution) { satisfaction=10; return; }
		satisfaction = 10-10*Math.abs(goalResolution-currentResolution)/goalResolution;
	}

	@Override
	public List<Transformation<?>> getTransformations() {
		ArrayList<Transformation<?>> tr = new ArrayList<Transformation<?>>();
		tr.add(new TDouglasPeuckerSimplifier(goalResolution));
		tr.add(new TTopologyPreservingSimplifier(goalResolution));
		tr.add(new TDouglasPeuckerSimplifier(goalResolution*0.7));
		tr.add(new TTopologyPreservingSimplifier(goalResolution*0.7));
		tr.add(new TDouglasPeuckerSimplifier(goalResolution*0.4));
		tr.add(new TTopologyPreservingSimplifier(goalResolution*0.4));
		tr.add(new TDouglasPeuckerSimplifier(goalResolution*0.2));
		tr.add(new TTopologyPreservingSimplifier(goalResolution*0.2));
		return tr;
	}

}
