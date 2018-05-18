/**
 * 
 */
package org.opencarto.algo.distances;

import org.opencarto.datamodel.Feature;

/**
 * @author julien Gaffuri
 *
 */
public class SemanticDistance implements Distance<Feature> {
	
	/* (non-Javadoc)
	 * @see org.opencarto.algo.distances.Distance#get(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double get(Feature f1, Feature f2) {
		//compare the attribute + values of the two features
		
		
		return 0;
	}

}
