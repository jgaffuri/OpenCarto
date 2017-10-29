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
			//return reconciliated results
			return null;
		} else {
			return run(features);
		}
	}


	private boolean partitionningNeeded(Collection<Feature> features) {
		return false;
	}


	public Object run(Collection<Feature> features) {
		return null;
	}

}
