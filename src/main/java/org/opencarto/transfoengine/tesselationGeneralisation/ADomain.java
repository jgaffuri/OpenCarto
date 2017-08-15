/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;

/**
 * @author julien Gaffuri
 *
 */
public class ADomain extends Agent {
	private ATesselation aTess;
	public ATesselation getAtesselation(){ return aTess; }

	public ADomain(Face object, ATesselation aTess) { super(object); this.aTess=aTess; }
	public Face getObject() { return (Face) super.getObject(); }



	public AUnit aUnit;

	public boolean isTheLastUnitPatchToRemove(){
		return aUnit.getNumberOfNonDeletedDomains() == 1;
	}

}
