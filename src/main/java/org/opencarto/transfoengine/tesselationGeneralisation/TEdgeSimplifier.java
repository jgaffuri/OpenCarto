/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.opencarto.algo.graph.EdgeScaling;
import org.opencarto.algo.graph.NodeDisplacement;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.TransformationCancellable;

/**
 * Generic class for edge geometry simplifiers.
 * 
 * @author julien Gaffuri
 *
 */
public abstract class TEdgeSimplifier extends TransformationCancellable<AEdge> {

	public TEdgeSimplifier(AEdge agent) { super(agent); }

	@Override
	public boolean isCancelable() { return true; }

	private LineString geomStore= null;
	private Coordinate closedEdgeNodePosition = null;
	protected double scaleRatio = 1;

	protected void postScaleClosed(Edge e, double targetArea) {
		if(e.getGeometry().isValid()){
			scaleRatio = Math.sqrt( targetArea / e.getArea() );
			scaleClosed(e);
		}
	}

	protected void scaleClosed(Edge e) {
		if(!e.isClosed() || scaleRatio == 1) return;
		EdgeScaling.scale(e, scaleRatio);
	}

	@Override
	public void storeState() {
		Edge e = getAgent().getObject();
		geomStore = e.getGeometry();
		if(e.isClosed()) closedEdgeNodePosition = new Coordinate(e.getN1().getC().x, e.getN1().getC().y);
	}

	@Override
	public void cancel() {
		Edge e = getAgent().getObject();

		if(e.getGeometry().isValid()){
			scaleRatio = 1/scaleRatio;
			scaleClosed(e);
		}

		e.setGeom(geomStore);
		if(e.isClosed()) NodeDisplacement.moveTo(e.getN1(), closedEdgeNodePosition.x, closedEdgeNodePosition.y);;
	}

}
