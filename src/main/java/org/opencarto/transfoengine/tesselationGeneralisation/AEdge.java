/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Agent;

import eu.europa.ec.eurostat.eurogeostat.datamodel.graph.Edge;

/**
 * @author julien Gaffuri
 *
 */
public class AEdge extends Agent {
	private ATesselation aTess;
	public ATesselation getAtesselation(){ return aTess; }

	public AEdge(Edge object, ATesselation aTess) { super(object); this.aTess=aTess; }
	public Edge getObject() { return (Edge) super.getObject(); }

	public void clear() {
		aTess = null;
	}

}
