package org.opencarto.algo.clustering;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.Feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;

public class FeatureClusteringIndex implements ClusteringIndex<Feature>{
	private SpatialIndex index;

	public FeatureClusteringIndex(ArrayList<Feature> fs, SpatialIndex index){
		this.index = index;
		//initialise spatial index
		for(Feature f : fs){
			Geometry g = f.getGeom();
			if(g==null) continue;
			index.insert(g.getEnvelopeInternal(), f);
		}
	}

	@Override
	public List<Feature> getCandidates(Feature f, double distance) {
		Geometry g = f.getGeom();
		if(g==null) return null;
		Envelope env = g.getEnvelopeInternal();
		env.expandBy(distance);
		return index.query(env);
	}

}
