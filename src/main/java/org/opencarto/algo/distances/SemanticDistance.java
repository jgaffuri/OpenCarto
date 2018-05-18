/**
 * 
 */
package org.opencarto.algo.distances;

import java.util.Collection;
import java.util.HashSet;

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

		//get all attribute keys
		Collection<String> keys = new HashSet<>();
		keys.addAll(f1.getProperties().keySet());
		keys.addAll(f2.getProperties().keySet());

		if(keys.size() == 0) return 0;

		int nbCommon = 0;
		for(String key : keys) {
			Object v1 = f1.get(key), v2 = f2.get(key);
			if((v1==null && v2==null)||v1.equals(v2)) nbCommon++;
		}

		return 1.0-nbCommon/keys.size();
	}

}
