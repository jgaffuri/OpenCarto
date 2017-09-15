/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * Generic class for edge geometry simplifiers.
 * 
 * @author julien Gaffuri
 *
 */
public abstract class TEdgeSimplifier extends Transformation<AEdge> {

	public TEdgeSimplifier(AEdge agent) { super(agent); }

	@Override
	public boolean isCancelable() { return true; }

	private LineString geomStore= null;
	private Coordinate closedEdgeNodePosition = null;
	protected double scaleRatio = 1;

	protected void scaleClosed(Edge e) {
		if(!e.isClosed() || scaleRatio == 1) return;
		e.scaleClosed(scaleRatio);
	}

	@Override
	public void storeState() {
		Edge e = agent.getObject();
		geomStore = e.getGeometry();
		if(e.isClosed()) closedEdgeNodePosition = new Coordinate(e.getN1().getC().x, e.getN1().getC().y);
	}

	@Override
	public void cancel() {
		Edge e = agent.getObject();

		scaleRatio = 1/scaleRatio;
		scaleClosed(e);

		e.setGeom(geomStore);
		if(e.isClosed()) e.getN1().moveTo(closedEdgeNodePosition.x, closedEdgeNodePosition.y);;
	}

}
