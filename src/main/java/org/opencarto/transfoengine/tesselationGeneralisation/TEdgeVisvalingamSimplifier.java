/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.algo.line.VWSimplifier;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeVisvalingamSimplifier extends Transformation<AEdge> {

	private double resolution;

	public TEdgeVisvalingamSimplifier(AEdge agent, double resolution) {
		super(agent);
		this.resolution = resolution;
	}

	@Override
	public void apply() {
		Edge e = (Edge) agent.getObject();
		LineString lsIni = e.getGeometry(), lsFin;

		lsFin = (LineString) VWSimplifier.simplify(lsIni, resolution);

		e.setGeom(lsFin);
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
