package org.opencarto.algo.clustering;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;

public class FeatureClusteringIndex implements ClusteringIndex<SimpleFeature>{
	private SpatialIndex index;
	private String geomAtt = "the_geom";

	public FeatureClusteringIndex(ArrayList<SimpleFeature> fs, SpatialIndex index, String geomAtt){
		this.geomAtt = geomAtt;
		this.index = index;
		//initialise spatial index
		for(SimpleFeature f : fs){
			Geometry g = (Geometry)f.getAttribute(geomAtt);
			if(g==null) continue;
			index.insert(g.getEnvelopeInternal(), f);
		}
	}

	public FeatureClusteringIndex(ArrayList<SimpleFeature> fs, SpatialIndex index){
		this(fs, index, "the_geom");
	}

	@Override
	public List<SimpleFeature> getCandidates(SimpleFeature f, double distance) {
		Geometry g = (Geometry)f.getAttribute(geomAtt);
		if(g==null) return null;
		Envelope env = g.getEnvelopeInternal();
		env.expandBy(distance);
		return index.query(env);
	}

}
