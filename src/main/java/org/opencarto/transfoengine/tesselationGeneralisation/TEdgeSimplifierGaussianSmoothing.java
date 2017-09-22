/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.line.GaussianSmoothing;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.util.Util;

import com.vividsolutions.jts.geom.LineString;

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
		double area = e.getArea();
		try {
			LineString out = GaussianSmoothing.get(e.getGeometry(), gaussianSmoothingSigmaParameter, resolution);
			e.setGeom(out);
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
