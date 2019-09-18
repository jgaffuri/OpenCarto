/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.simplify.VWSimplifier;
import org.opencarto.algo.graph.GraphUtils;
import org.opencarto.algo.line.GaussianLineSmoothing;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.util.Util;

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
		Edge e = getAgent().getObject();
		double area = GraphUtils.getArea(e);

		//apply VW filter
		LineString out = (LineString) VWSimplifier.simplify(e.getGeometry(), resolution);

		//apply gaussian smoothing
		if(gaussianSmoothingSigmaParameter > 0)
			out = GaussianLineSmoothing.get(out, gaussianSmoothingSigmaParameter, resolution);

		e.setGeom(out.getCoordinates());

		//scale closed lines
		postScaleClosed(e, area);
	}

	public String toString(){
		return getClass().getSimpleName() + "(res="+Util.round(resolution, 3)+";gaus="+Util.round(gaussianSmoothingSigmaParameter, 3)+")";
	}
}
