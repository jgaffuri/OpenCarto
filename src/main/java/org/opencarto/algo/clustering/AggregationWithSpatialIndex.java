/**
 * 
 */
package org.opencarto.algo.clustering;

import com.vividsolutions.jts.index.SpatialIndex;

/**
 * @author julien Gaffuri
 *
 */
public abstract class AggregationWithSpatialIndex<T> implements Aggregation<T> {
	public SpatialIndex index = null;
}
