package org.opencarto.algo.distances;

import org.opencarto.util.ProjectionUtil;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Distance for simple features.
 * 
 * @author julien Gaffuri
 *
 */
public class FeatureDistance implements Distance<SimpleFeature> {
	private String geomAtt = "the_geom";
	private boolean projected = true;

	public FeatureDistance() {}
	public FeatureDistance(String geomAtt){ this.geomAtt = geomAtt; }
	public FeatureDistance(boolean projected){ this.projected = projected; }
	public FeatureDistance(String geomAtt, boolean projected){
		this.geomAtt = geomAtt;
		this.projected = projected;
	}

	@Override
	public double get(SimpleFeature f1, SimpleFeature f2) {
		Geometry g1 = (Geometry)f1.getAttribute(geomAtt);
		Geometry g2 = (Geometry)f2.getAttribute(geomAtt);
		if(!projected){
			g1 = ProjectionUtil.toWebMercator(g1, ProjectionUtil.getWGS_84_CRS());
			g2 = ProjectionUtil.toWebMercator(g2, ProjectionUtil.getWGS_84_CRS());
		}
		//System.out.println(g1.distance(g2));
		return g1.distance(g2);
	}

}
