/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;

/**
 * Ensures that the face contains some specified points.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceContainPoints extends Constraint<AFace> {

	public CFaceContainPoints(AFace agent) {
		super(agent);
	}

	@Override
	public void computeSatisfaction() {
		Face f = getAgent().getObject();
		satisfaction = f.containPoints(getAgent().points)? 10 : 0;
	}

	@Override
	public boolean isHard() { return true; }


	/*/usefull to instanciate the constraint
	public static Collection<Point> getPointsInFace(AFace a, HashMap<String, Collection<Point>> ptsIndex) {
		if(a==null) return null;
		if(a.aUnit==null) return null;
		Collection<Point> pts = ptsIndex.get(a.aUnit.getId());
		if(pts == null || pts.size()==0) return null;
		Collection<Point> ptsF = new ArrayList<Point>();
		if(pts!=null) for(Point pt : pts) if(a.getObject().getGeometry().contains(pt)) ptsF.add(pt);
		if(ptsF.size()==0) return null;
		return ptsF;
	}*/

}
