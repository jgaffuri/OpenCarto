/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.algo.measure.Granularity;
import org.opencarto.algo.measure.Granularity.GranularityMeasurement;
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
	boolean noTriangle = false;

	public CEdgeGranularity(Agent agent, double goalResolution, boolean noTriangle) {
		super(agent);
		this.goalResolution = goalResolution;
		this.noTriangle = noTriangle;
	}

	@Override
	public void computeCurrentValue() {
		LineString g = ((Edge)getAgent().getObject()).getGeometry();
		GranularityMeasurement m = Granularity.get(g, goalResolution);
		if(Double.isNaN(m.averageBelow)) currentResolution = m.average;
		else currentResolution = m.averageBelow;
	}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) { satisfaction=10; return; }

		LineString g = ((Edge)getAgent().getObject()).getGeometry();
		if(noTriangle && g.isClosed() && g.getNumPoints()<=5) { satisfaction=10; return; }
		//if(g.getNumPoints()==2) { satisfaction=10; return; } //only edge deletion could handle such case. See constraint on edge minimum size.

		if(currentResolution>=goalResolution) { satisfaction=10; return; }
		satisfaction = 10-10*Math.abs(goalResolution-currentResolution)/goalResolution;
	}

	@Override
	public List<Transformation<?>> getTransformations() {
		ArrayList<Transformation<?>> tr = new ArrayList<Transformation<?>>();

		Edge e = ((AEdge)getAgent()).getObject();
		double length = e.getGeometry()==null? 0 : e.getGeometry().getLength();

		if(length<=goalResolution){
			//tr.add(new TEdgeCollapse((AEdge) getAgent())); //TODO ensure faces remain valid after edge collapse
			//TODO add also edge lengthening?
		} else {
			double[] ks = new double[]{1,0.8,0.6,0.4,0.2,0.1};

			//tr.add(new TEdgeGaussianSmoothing((AEdge) getAgent(), goalResolution*0.01, goalResolution));

			for(double k : ks)
				tr.add(new TEdgeVisvalingamSimplifier((AEdge) getAgent(), k*goalResolution));

			/*for(double k : ks){
				tr.add(new TEdgeRamerDouglasPeuckerSimplifier((AEdge) getAgent(), k*goalResolution, false));
				tr.add(new TEdgeRamerDouglasPeuckerSimplifier((AEdge) getAgent(), k*goalResolution, true));
			}*/
		}

		return tr;
	}

}
