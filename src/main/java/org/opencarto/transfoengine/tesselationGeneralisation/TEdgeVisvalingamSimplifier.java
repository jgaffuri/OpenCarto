/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.line.GaussianSmoothing;
import org.opencarto.algo.line.VWSimplifier;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.util.Util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeVisvalingamSimplifier extends Transformation<AEdge> {

	private double resolution, gaussianSmoothingSigmaParameter=-1;

	public TEdgeVisvalingamSimplifier(AEdge agent, double resolution) { this(agent, resolution, resolution); }
	public TEdgeVisvalingamSimplifier(AEdge agent, double resolution, double gaussianSmoothingSigmaParameter) {
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




	@Override
	public boolean isCancelable() { return true; }

	private LineString geomStore= null;
	private Coordinate closedEdgeNodePosition = null;

	@Override
	public void storeState() {
		Edge e = agent.getObject();
		geomStore = e.getGeometry();
		if(e.isClosed()) closedEdgeNodePosition = new Coordinate(e.getN1().getC().x, e.getN1().getC().y);
	}

	@Override
	public void cancel() {
		Edge e = agent.getObject();
		e.setGeom(geomStore);
		if(e.isClosed()) e.getN1().moveTo(closedEdgeNodePosition.x, closedEdgeNodePosition.y);;
	}

	public String toString(){
		return getClass().getSimpleName() + "(res="+Util.round(resolution, 3)+";gaus="+Util.round(gaussianSmoothingSigmaParameter, 3)+")";
	}
}
