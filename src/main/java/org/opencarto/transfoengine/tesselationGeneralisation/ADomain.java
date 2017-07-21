/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.State;

/**
 * @author julien Gaffuri
 *
 */
public class ADomain extends Agent {

	public ADomain(Object object) { super(object); }
	public Domain getObject() { return (Domain) super.getObject(); }

	@Override
	public State getState() {
		return null;
	}

	@Override
	public void goBackTo(State state) {}

}
