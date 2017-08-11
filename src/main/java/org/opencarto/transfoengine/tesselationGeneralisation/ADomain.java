/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Domain;
import org.opencarto.transfoengine.Agent;

/**
 * @author julien Gaffuri
 *
 */
public class ADomain extends Agent {

	public ADomain(Domain object) { super(object); }
	public Domain getObject() { return (Domain) super.getObject(); }

}
