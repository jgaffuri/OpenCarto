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

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayGeneralisation {
	public final static Logger LOGGER = Logger.getLogger(MainRailwayGeneralisation.class.getName());

	public static void main(String[] args) throws Exception {

		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";

		int quad = 5;
		double trackAxisSpacing = 6;
		double resServAreas = trackAxisSpacing * 2;
		double sizeSel = trackAxisSpacing*trackAxisSpacing*5;


		LOGGER.info("Load input sections");
		ArrayList<Feature> secs = SHPUtil.loadSHP(inFile).fs;
		System.out.println(secs.size()+"   "+FeatureUtil.getVerticesNumber(secs));

		LOGGER.info("Buffer-union of all geometries");
		Geometry g = new BufferAggregation(trackAxisSpacing, -1, quad, -1, false).aggregateGeometries(FeatureUtil.getGeometries(secs));
		LOGGER.info("Buffer in");
		g = g.buffer(-trackAxisSpacing);

		LOGGER.info("Buffers for service areas");
		Geometry servArea = g.buffer(-resServAreas, quad).buffer(resServAreas*1.001, quad);
		LOGGER.info("Difference for double tracks");
		Geometry doubleTrack = g.difference(servArea);
		g = null;

		LOGGER.info("Decomposition into polygons + filter by size");
		Collection<?> servAreas = JTSGeomUtil.getPolygonGeometries(servArea, sizeSel);
		servArea = null;
		Collection<?> doubleTracks = JTSGeomUtil.getPolygonGeometries(doubleTrack, sizeSel);
		doubleTrack = null;

		LOGGER.info("   nbAreas = " + servAreas.size() + "   nbDoubleTracks = " + doubleTracks.size());

		LOGGER.info("Save");
		SHPUtil.saveGeomsSHP((Collection<Geometry>) servAreas, basePath+"out/service_areas.shp", SHPUtil.getCRS(inFile));
		SHPUtil.saveGeomsSHP((Collection<Geometry>) doubleTracks, basePath+"out/double_tracks.shp", SHPUtil.getCRS(inFile));

		System.out.println("End");
	}


}
