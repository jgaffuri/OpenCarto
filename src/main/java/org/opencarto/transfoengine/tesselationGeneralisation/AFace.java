/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

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



	public AUnit aUnit = null;

	public boolean removalAllowed(){
		if(aUnit == null) return true;
		return aUnit.getNumberOfNonDeletedFaces() > 1;
	}

	public boolean isHole() {
		return aUnit == null;
	}

	public boolean hasFrozenEdge() {
		for(Edge e : getObject().getEdges())
			if (aTess.getAEdge(e).isFrozen()) return true;
		return false;
	}

	public void clear() {
		aTess = null;
		aUnit = null;
	}

}
