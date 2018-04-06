/**
 * 
 */
package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.ejml.alg.block.GeneratorBlockInnerMultiplication;
import org.opencarto.algo.aggregation.BufferAggregation;
import org.opencarto.algo.graph.GraphConnexComponents;
import org.opencarto.algo.measure.Circularity;
import org.opencarto.algo.measure.Elongation;
import org.opencarto.algo.measure.Elongation.WidthApproximation;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.GraphSHPUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * Some tests on ORM generalisation
 * 
 * @author julien Gaffuri
 *
 */
public class MainORMGeneBuildServiceAreas {
	public final static Logger LOGGER = Logger.getLogger(MainORMGeneBuildServiceAreas.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");


		LOGGER.info("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		int epsg = 3035;
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/orm_tracks.shp", epsg).fs;

		LOGGER.info("Build areas");
		double res = 10.0; //for 1:50k
		Geometry area = new BufferAggregation(res, res, 5, -1, false).aggregateGeometries(FeatureUtil.getGeometries(tracks));
		Collection<?> areas = JTSGeomUtil.getPolygonGeometries(area);
		area = null;
		LOGGER.info("   "+areas.size());

		LOGGER.info("Save");
		SHPUtil.saveGeomsSHP((Collection<Geometry>) areas, epsg, basePath+"out/", "areas.shp");

		System.out.println("End");
	}

}
