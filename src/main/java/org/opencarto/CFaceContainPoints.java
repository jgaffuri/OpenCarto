/**
 * 
 */
package org.opencarto;

import java.util.Collection;
import java.util.HashMap;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
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
		ok = checkFace(f, ptData.get(f.getId()));
	}

	static boolean checkFace(Face f, Collection<Point> pts) {
		if(pts == null) return true;
		for(Point pt : pts)
			if(! f.getGeometry().contains(pt)) return false;
		return true;
	}


	@Override
	public void computeSatisfaction() {
		satisfaction = ok? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
