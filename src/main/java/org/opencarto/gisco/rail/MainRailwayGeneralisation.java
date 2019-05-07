/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.opencarto.algo.aggregation.BufferAggregation;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;
import org.opencarto.util.ProjectionUtil;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayGeneralisation {
	public final static Logger LOGGER = Logger.getLogger(MainRailwayGeneralisation.class.getName());

	public static void main(String[] args) throws Exception {
		int quad = 5;
		double res = 10; //for 1:50k
		double areaBufferParamM = 7;

		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		ArrayList<Feature> sections = SHPUtil.loadSHP(basePath+"out/EM/RailwayLinkEM.shp").fs;
		System.out.println(sections.size()+"   "+FeatureUtil.getVerticesNumber(sections));

		LOGGER.info("Glue buffer out");
		Geometry glue = new BufferAggregation(res, -1, quad, -1, false).aggregateGeometries(FeatureUtil.getGeometries(sections));
		LOGGER.info("Glue buffer in");
		glue = glue.buffer(-res);

		LOGGER.info("Buffers for service areas");
		Geometry sArea = glue.buffer(-areaBufferParamM, quad).buffer(areaBufferParamM*1.001, quad);
		LOGGER.info("Difference for double tracks");
		Geometry doubleTrack = glue.difference(sArea);
		glue = null;

		LOGGER.info("Decomposition into polygons + filter by size");
		double sizeSel = res*res*5;
		Collection<?> areas = JTSGeomUtil.getPolygonGeometries(sArea, sizeSel); sArea = null;
		Collection<?> doubleTracks = JTSGeomUtil.getPolygonGeometries(doubleTrack, sizeSel); doubleTrack = null;

		LOGGER.info("   nbAreas = "+areas.size() + "   nbDoubleTracks = " + doubleTracks.size());

		LOGGER.info("Save");
		SHPUtil.saveGeomsSHP((Collection<Geometry>) areas, basePath+"out/service_areas.shp", ProjectionUtil.getETRS89_LAEA_CRS());
		SHPUtil.saveGeomsSHP((Collection<Geometry>) doubleTracks, basePath+"out/double_tracks.shp", ProjectionUtil.getETRS89_LAEA_CRS());

		System.out.println("End");
	}


}
