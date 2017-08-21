/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.line.GaussianSmoothing;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeGaussianSmoothing extends Transformation<AEdge> {

	private double gaussianSmoothingSigmaParameter, resolution;

	public TEdgeGaussianSmoothing(AEdge agent, double gaussianSmoothingSigmaParameter, double resolution) {
		super(agent);
		this.gaussianSmoothingSigmaParameter = gaussianSmoothingSigmaParameter;
		this.resolution = resolution;
	}

	@Override
	public void apply() {
		Edge e = (Edge) agent.getObject();
		LineString out = GaussianSmoothing.get(e.getGeometry(), gaussianSmoothingSigmaParameter, resolution);
		e.setGeom(out);
	}





	@Override
	public boolean isCancelable() { return true; }

	private LineString geomStore= null;

	@Override
	public void storeState() {
		geomStore = ((Edge)agent.getObject()).getGeometry();
	}

	@Override
	public void cancel() {
		((Edge)agent.getObject()).setGeom(geomStore);
	}

}
