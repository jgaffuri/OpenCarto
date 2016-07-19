package org.opencarto.algo.clustering;

import java.util.Collection;

public interface ClusteringIndex<T> {
	Collection<T> getCandidates(T obj, double distance);
}
