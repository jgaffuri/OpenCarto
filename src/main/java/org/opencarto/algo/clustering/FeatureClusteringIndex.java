package org.opencarto.algo.clustering;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.SpatialIndex;
import org.opencarto.datamodel.Feature;

public class FeatureClusteringIndex implements ClusteringIndex<Feature>{
	private SpatialIndex index;

	public FeatureClusteringIndex(ArrayList<Feature> fs, SpatialIndex index){
		this.index = index;
		//initialise spatial index
		for(Feature f : fs){
			Geometry g = f.getDefaultGeometry();
			if(g==null) continue;
			index.insert(g.getEnvelopeInternal(), f);
		}
	}

	public List<Feature> getCandidates(Feature f, double distance) {
		Geometry g = f.getDefaultGeometry();
		if(g==null) return null;
		Envelope env = g.getEnvelopeInternal();
		env.expandBy(distance);
		return index.query(env);
	}

}
