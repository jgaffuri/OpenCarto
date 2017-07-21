/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.State;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class AEdge extends Agent {

	public AEdge(Object object) { super(object); }
	public Edge getObject() { return (Edge) super.getObject(); }


	@Override
	public State getState() {
		return new State(getObject().getGeometry());
	}

	@Override
	public void goBackTo(State state) {
		getObject().setGeom((LineString) state.geom);
	}

}
