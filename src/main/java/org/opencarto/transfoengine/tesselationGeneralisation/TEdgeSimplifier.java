/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.Transformation;

/**
 * Generic class for edge geometry simplifiers.
 * 
 * @author julien Gaffuri
 *
 */
public abstract class TEdgeSimplifier extends Transformation<AEdge> {

	public TEdgeSimplifier(AEdge agent) { super(agent); }


	//TODO transfer here from sub classes
}
