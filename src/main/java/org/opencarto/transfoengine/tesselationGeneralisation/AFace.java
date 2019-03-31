/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.locationtech.jts.geom.Point;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;

/**
 * @author julien Gaffuri
 *
 */
public class AFace extends Agent {
	private ATesselation aTess;
	public ATesselation getAtesselation(){ return aTess; }

	public AFace(Face object, ATesselation aTess) { super(object); this.aTess=aTess; }
	public Face getObject() { return (Face) super.getObject(); }

	//the points that are supposed to be inside the face, and might be used for a constraint
	public Collection<Point> points = null;


	public AUnit aUnit = null;

	public boolean lastUnitFace(){
		if(aUnit == null) return false;
		return aUnit.getNumberOfNonDeletedFaces() == 1;
	}

	public boolean isHole() {
		return aUnit == null;
	}

	public boolean hasFrozenEdge() {
		for(Edge e : getObject().getEdges())
			if (aTess.getAEdge(e).isFrozen()) return true;
		return false;
	}

	//check if the face contains its points
	public boolean containPoints(boolean checkAlsoNeigbours) {
		if(points == null || points.size()==0) return true;
		for(Point pt : points)
			if(! getObject().getGeom().contains(pt)) return false;
		if(checkAlsoNeigbours)
			for(Face f : getObject().getTouchingFaces())
				if(!aTess.getAFace(f).containPoints(false)) return false;
		return true;
	}

	public void clear() {
		aTess = null;
		aUnit = null;
		if(points != null) points.clear(); points = null;
	}

}
