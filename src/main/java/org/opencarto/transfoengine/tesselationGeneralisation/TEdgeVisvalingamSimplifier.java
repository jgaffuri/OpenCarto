/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.line.GaussianSmoothing;
import org.opencarto.algo.line.VWSimplifier;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeVisvalingamSimplifier extends Transformation<AEdge> {

	private double resolution, gaussianSmoothingSigmaParameter=-1;

	public TEdgeVisvalingamSimplifier(AEdge agent, double resolution) { this(agent, resolution, -1); }
	public TEdgeVisvalingamSimplifier(AEdge agent, double resolution, double gaussianSmoothingSigmaParameter) {
		super(agent);
		this.resolution = resolution;
		this.gaussianSmoothingSigmaParameter = gaussianSmoothingSigmaParameter;
	}

	@Override
	public void apply() {
		Edge e = (Edge) agent.getObject();

		LineString out = (LineString) VWSimplifier.simplify(e.getGeometry(), resolution);
		if(gaussianSmoothingSigmaParameter > 0)
			out = GaussianSmoothing.get(out, gaussianSmoothingSigmaParameter, resolution);

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
