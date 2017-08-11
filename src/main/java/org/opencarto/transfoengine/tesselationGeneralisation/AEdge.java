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

	public AEdge(Object object) { super(object); }
	public Edge getObject() { return (Edge) super.getObject(); }

}
