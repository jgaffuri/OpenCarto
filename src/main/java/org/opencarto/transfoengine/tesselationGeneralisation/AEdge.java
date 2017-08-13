/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;

/**
 * @author julien Gaffuri
 *
 */
public class AEdge extends Agent {
	private ATesselation aTess;
	public ATesselation getAtesselation(){ return aTess; }

	public AEdge(Edge object, ATesselation aTess) { super(object); this.aTess=aTess; }
	public Edge getObject() { return (Edge) super.getObject(); }

}
