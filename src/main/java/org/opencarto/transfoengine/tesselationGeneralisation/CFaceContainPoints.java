/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;

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


	//usefull to instanciate the constraint
	public static Collection<Point> getPointsInFace(AFace a, HashMap<String, Collection<Point>> ptsIndex) {
		if(a.aUnit==null) return null;
		Collection<Point> pts = ptsIndex.get(a.aUnit.getId());
		if(pts == null || pts.size()==0) return null;
		Collection<Point> ptsF = new ArrayList<Point>();
		if(pts!=null) for(Point pt : pts) if(a.getObject().getGeometry().contains(pt)) ptsF.add(pt);
		if(ptsF.size()==0) return null;
		return ptsF;
	}

}
