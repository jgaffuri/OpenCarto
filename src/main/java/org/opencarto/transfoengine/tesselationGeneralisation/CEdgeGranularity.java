/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.algo.measure.Granularity;
import org.opencarto.algo.measure.Granularity.Measurement;
import org.opencarto.algo.resolutionise.Resolutionise;
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
		Measurement m = Granularity.get(g, goalResolution);
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

		Edge e = (Edge)(getAgent().getObject());
		double length = e.getGeometry()==null? 0 : e.getGeometry().getLength();

		if(length<=goalResolution){
			//TODO improve test with number of points?
			//TODO add edge collapse and edge lengthening
		} else {
			tr.add(new TVisvalingamSimplifier((AEdge) getAgent(), goalResolution));
			tr.add(new TVisvalingamSimplifier((AEdge) getAgent(), goalResolution*0.7));
			tr.add(new TVisvalingamSimplifier((AEdge) getAgent(), goalResolution*0.4));
			tr.add(new TVisvalingamSimplifier((AEdge) getAgent(), goalResolution*0.2));

			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution, false));
			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution, true));
			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution*0.7, false));
			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution*0.7, true));
			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution*0.4, false));
			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution*0.4, true));
			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution*0.2, false));
			tr.add(new TDouglasPeuckerSimplifier((AEdge) getAgent(), goalResolution*0.2, true));
		}

		return tr;
	}

}
