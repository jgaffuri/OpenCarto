/**
 * 
 */
package org.opencarto;

import java.util.Collection;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;

import com.vividsolutions.jts.geom.Point;

/**
 * Ensures that the face contains some specified points.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgesFacesContainPoints extends Constraint<AEdge> {

	//dictionnary giving for each face id the collection of points to consider
	private Collection<Point> ptDataF1 = null;
	private Collection<Point> ptDataF2 = null;

	public CEdgesFacesContainPoints(AEdge agent, Collection<Point> ptDataF1, Collection<Point> ptDataF2) {
		super(agent);
		this.ptDataF1 = ptDataF1;
		this.ptDataF2 = ptDataF2;
	}

	@Override
	public void computeSatisfaction() {
		Edge e = getAgent().getObject();
		boolean ok = CFaceContainPoints.checkFace(e.f1, ptDataF1) && CFaceContainPoints.checkFace(e.f2, ptDataF2);
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
