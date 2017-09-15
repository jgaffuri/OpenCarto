/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.line.GaussianSmoothing;
import org.opencarto.algo.line.VWSimplifier;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.util.Util;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeSimplifierVisvalingamWhyatt extends TEdgeSimplifier {

	private double resolution, gaussianSmoothingSigmaParameter=-1;

	public TEdgeSimplifierVisvalingamWhyatt(AEdge agent, double resolution) { this(agent, resolution, resolution); }
	public TEdgeSimplifierVisvalingamWhyatt(AEdge agent, double resolution, double gaussianSmoothingSigmaParameter) {
		super(agent);
		this.resolution = resolution;
		this.gaussianSmoothingSigmaParameter = gaussianSmoothingSigmaParameter;
	}

	@Override
	public void apply() {
		Edge e = agent.getObject();

		//apply VW filter
		LineString out = (LineString) VWSimplifier.simplify(e.getGeometry(), resolution);

		//apply gaussian smoothing
		if(gaussianSmoothingSigmaParameter > 0)
			out = GaussianSmoothing.get(out, gaussianSmoothingSigmaParameter, resolution);

		if(e.isClosed()){
			//TODO apply scaling
		}

		e.setGeom(out);
	}


	public String toString(){
		return getClass().getSimpleName() + "(res="+Util.round(resolution, 3)+";gaus="+Util.round(gaussianSmoothingSigmaParameter, 3)+")";
	}
}
