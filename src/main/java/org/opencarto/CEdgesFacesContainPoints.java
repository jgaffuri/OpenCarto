/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;

import com.vividsolutions.jts.geom.Point;

/**
 * Ensures that the face contains some specified points.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgesFacesContainPoints extends Constraint<AEdge> {

	//dictionnary giving for each face id the collection of points to consider
	private HashMap<String, Collection<Point>> ptData = null;

	public CEdgesFacesContainPoints(AEdge agent, HashMap<String, Collection<Point>> ptData) {
		super(agent);
		this.ptData = ptData;
	}

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		Edge e = getAgent().getObject();
		ok = CFaceContainPoints.checkFace(e.f1, ptData.get(e.f1.getId())) && CFaceContainPoints.checkFace(e.f2, ptData.get(e.f2.getId()));
	}


	@Override
	public void computeSatisfaction() {
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		return new ArrayList<Transformation<AEdge>>();
	}
}
