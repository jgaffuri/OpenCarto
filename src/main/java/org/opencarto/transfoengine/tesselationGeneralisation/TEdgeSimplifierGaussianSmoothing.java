/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.locationtech.jts.geom.LineString;
import org.opencarto.algo.graph.GraphUtils;
import org.opencarto.algo.line.GaussianLineSmoothing;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeSimplifierGaussianSmoothing extends TEdgeSimplifier {

	private double gaussianSmoothingSigmaParameter, resolution;

	public TEdgeSimplifierGaussianSmoothing(AEdge agent, double gaussianSmoothingSigmaParameter, double resolution) {
		super(agent);
		this.gaussianSmoothingSigmaParameter = gaussianSmoothingSigmaParameter;
		this.resolution = resolution;
	}

	@Override
	public void apply() {
		Edge e = getAgent().getObject();
		double area = GraphUtils.getArea(e);
		try {
			LineString out = GaussianLineSmoothing.get(e.getGeometry(), gaussianSmoothingSigmaParameter, resolution);
			e.setGeom(out.getCoordinates());
			//scale closed lines
			postScaleClosed(e, area);
		} catch (Exception e1) {
			System.err.println("Gaussian smoothing failed for "+getAgent().getId());
			//e1.printStackTrace();
		}
	}



	public String toString(){
		return getClass().getSimpleName() + "(sig="+Util.round(gaussianSmoothingSigmaParameter, 3)+";res="+Util.round(resolution, 3)+")";
	}
}
