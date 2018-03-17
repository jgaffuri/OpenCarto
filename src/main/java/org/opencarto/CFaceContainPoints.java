/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;

import com.vividsolutions.jts.geom.Point;

/**
 * Ensures that the face contains some specified points.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceContainPoints extends Constraint<AFace> {

	//dictionnary giving for each face id the collection of points to consider
	private HashMap<String, Collection<Point>> ptData = null;

	public CFaceContainPoints(AFace agent, HashMap<String, Collection<Point>> ptData) {
		super(agent);
		this.ptData = ptData;
	}

	private boolean ok = true;

	@Override
	public void computeCurrentValue() {
		Face f = getAgent().getObject();

		//get the points to check
		Collection<Point> pts = ptData.get(f.getId());
		if(pts == null) return;

		//check the points
		ok = true;
		for(Point pt : pts) {
			if(f.getGeometry().contains(pt)) continue;
			ok = false;
			return;
		}
	}


	@Override
	public void computeSatisfaction() {
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<AFace>> getTransformations() {
		return new ArrayList<Transformation<AFace>>();
	}
}
