/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.Collection;

import org.opencarto.datamodel.Feature;

/**
 * 
 * An utility to run partitionned processes.
 * 
 * @author julien Gaffuri
 *
 */
public class Partitionner {

	public Object runRecurssively(Collection<Feature> features) {
		if (partitionningNeeded(features)) {
			//decompose in four parts
			//launch job on parts
			//reconciliate result
		} else {
			return run(features);
		}
	}


	public Object run(Collection<Feature> features) {
		return null;
	}

}
