/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;

/**
 * 
 * An utility to run partitionned processes.
 * 
 * @author julien Gaffuri
 *
 */
public class GeneralisationPartitionner {

	//TODO need for partition object?

	public Object runRecurssively(Collection<Feature> features) {
		if (partitionningNeeded(features)) {
			//partiionning
			Collection<Collection<Feature>> partitions = partition(features);
			//launch job on parts
			Collection<Object> results = new HashSet<Object>();
			for(Collection<Feature> partition : partitions) results.add(run(partition));
			//return reconciliated results
			return reconciliate(results);
		} else {
			return run(features);
		}
	}

	private boolean partitionningNeeded(Collection<Feature> features) {
		//TODO
		return false;
	}

	private Collection<Collection<Feature>> partition(Collection<Feature> features) {
		//TODO
		return null;
	}

	public Object run(Collection<Feature> features) {
		//TODO
		return null;
	}

	private Object reconciliate(Collection<Object> results) {
		//TODO
		return null;
	}

}
