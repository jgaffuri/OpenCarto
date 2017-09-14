/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.algo.measure.Granularity;
import org.opencarto.algo.measure.Granularity.GranularityMeasurement;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * Ensure the granularity of an edge is below a target resolution value.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeGranularity extends Constraint<AEdge> {
	double goalResolution, currentResolution;
	boolean noTriangle = false;

	public CEdgeGranularity(AEdge agent, double goalResolution, boolean noTriangle) {
		super(agent);
		this.goalResolution = goalResolution;
		this.noTriangle = noTriangle;
	}

	@Override
	public void computeCurrentValue() {
		LineString g = getAgent().getObject().getGeometry();
		GranularityMeasurement m = Granularity.get(g, goalResolution);
		if(Double.isNaN(m.averageBelow)) currentResolution = m.average;
		else currentResolution = m.averageBelow;
	}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) { satisfaction=10; return; }

		LineString g = getAgent().getObject().getGeometry();

		//case of triangle
		if(g.isClosed() && noTriangle && g.getNumPoints()<=5) { satisfaction=10; return; }

		//case of segment
		if(g.getNumPoints()==2) { satisfaction=10; return; }

		if(currentResolution>=goalResolution)
			satisfaction=10;
		else
			satisfaction = 10-10*Math.abs(goalResolution-currentResolution)/goalResolution;
	}

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		ArrayList<Transformation<AEdge>> tr = new ArrayList<Transformation<AEdge>>();

		//Edge e = ((AEdge)getAgent()).getObject();
		//double length = e.getGeometry()==null? 0 : e.getGeometry().getLength();
		//if(length<=goalResolution){
		//tr.add(new TEdgeCollapse((AEdge) getAgent())); //TODO ensure faces remain valid after edge collapse
		//} else {
		double[] ks = new double[]{1,0.75,0.5,0.2,0.1};

		for(double k : ks)
			tr.add(new TEdgeVisvalingamSimplifier((AEdge) getAgent(), k*goalResolution));
		for(double k : ks){
			//tr.add(new TEdgeRamerDouglasPeuckerSimplifier((AEdge) getAgent(), k*goalResolution, false));
			tr.add(new TEdgeRamerDouglasPeuckerSimplifier((AEdge) getAgent(), k*goalResolution, true));
		}

		return tr;
	}

}
