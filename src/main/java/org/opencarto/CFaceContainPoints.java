/**
 * 
 */
package org.opencarto;

import java.util.Collection;

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
	private Collection<Point> ptData = null;

	public CFaceContainPoints(AFace agent, Collection<Point> ptData) {
		super(agent);
		this.ptData = ptData;
	}

	static boolean checkFaceContainPoints(Face f, Collection<Point> pts) {
		if(pts == null || f == null) return true;
		for(Point pt : pts)
			if(! f.getGeometry().contains(pt)) return false;
		return true;
	}

	@Override
	public void computeSatisfaction() {
		Face f = getAgent().getObject();
		satisfaction = checkFaceContainPoints(f, ptData)? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }

}
