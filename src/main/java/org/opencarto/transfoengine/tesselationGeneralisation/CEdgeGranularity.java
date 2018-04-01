/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.algo.measure.Granularity;
import org.opencarto.algo.measure.Granularity.GranularityMeasurement;
import org.opencarto.algo.polygon.Triangle;
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
	double goalGranularity, currentGranularity;
	boolean noTriangle = false;

	public CEdgeGranularity(AEdge agent, double goalResolution, boolean noTriangle) {
		super(agent);
		this.goalGranularity = goalResolution;
		this.noTriangle = noTriangle;
	}

	@Override
	public void computeCurrentValue() {
		GranularityMeasurement m = Granularity.get(getAgent().getObject().getGeometry(), goalGranularity);
		if(Double.isNaN(m.averageBelow)) currentGranularity = m.average;
		else currentGranularity = m.averageBelow;
	}

	@Override
	public void computeSatisfaction() {
		LineString ls = getAgent().getObject().getGeometry();
		//case of segment
		if(ls.getNumPoints()==2) { satisfaction=10; return; }
		//case of triangle
		if(noTriangle && Triangle.is(ls)) { satisfaction=10; return; }
		//case when granularity is ok
		if(currentGranularity >= goalGranularity) { satisfaction=10; return; }
		//general case
		satisfaction = 10-10*Math.abs(goalGranularity-currentGranularity)/goalGranularity;
	}

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		ArrayList<Transformation<AEdge>> out = new ArrayList<Transformation<AEdge>>();

		//Edge e = getAgent().getObject();
		//double length = e.getGeometry()==null? 0 : e.getGeometry().getLength();
		//if(length<=goalResolution){
		//tr.add(new TEdgeCollapse(getAgent())); //TODO ensure faces remain valid after edge collapse
		//} else {

		double[] ks = new double[]{ 1, 0.8, 0.6, 0.4, 0.2 };
		for(double k : ks)
			out.add(new TEdgeSimplifierVisvalingamWhyatt(getAgent(), k*goalGranularity));
		/*for(double k : ks){
			//tr.add(new TEdgeRamerDouglasPeuckerSimplifier(getAgent(), k*goalGranularity, false));
			out.add(new TEdgeSimplifierRamerDouglasPeucker(getAgent(), k*goalGranularity, true));
		}*/

		return out;
	}

}
