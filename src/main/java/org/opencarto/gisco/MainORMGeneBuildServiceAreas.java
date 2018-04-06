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
		int quad = 5;

		LOGGER.info("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		int epsg = 3035;
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/orm_tracks.shp", epsg).fs;

		LOGGER.info("Glue buffer out");
		double res = 10; //for 1:50k
		Geometry glue = new BufferAggregation(res, -1, quad, -1, false).aggregateGeometries(FeatureUtil.getGeometries(tracks));
		LOGGER.info("Glue buffer in");
		glue = glue.buffer(-res);

		LOGGER.info("Buffers for service areas");
		double areaParamM = 7;
		Geometry sArea = glue.buffer(-areaParamM,quad).buffer(areaParamM*1.001,quad);
		LOGGER.info("Difference for double tracks");
		Geometry doubleTrack = glue.difference(sArea);
		glue = null;

		LOGGER.info("Decomposition into polygons + filter by size");
		double sizeSel = res*res*5;
		Collection<?> areas = JTSGeomUtil.getPolygonGeometries(sArea, sizeSel); sArea = null;
		Collection<?> doubleTracks = JTSGeomUtil.getPolygonGeometries(doubleTrack, sizeSel); doubleTrack = null;

		LOGGER.info("   nbAreas="+areas.size() + "   nbDoubleTracks=" + doubleTracks.size());

		LOGGER.info("Save");
		SHPUtil.saveGeomsSHP((Collection<Geometry>) areas, epsg, basePath+"out/", "areas.shp");
		SHPUtil.saveGeomsSHP((Collection<Geometry>) doubleTracks, epsg, basePath+"out/", "doubleTracks.shp");

		System.out.println("End");
	}

}
