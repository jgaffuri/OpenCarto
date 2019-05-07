/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.opencarto.algo.aggregation.BufferAggregation;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;

/**
 * @author julien Gaffuri
 *
 */
public class RailwayAreasDetection {

	public final static Logger LOGGER = Logger.getLogger(RailwayAreasDetection.class.getName());

	private Collection<Feature> secs;

	private int quad = 5;
	private double trackAxisSpacing = 5;
	private double resServAreas = trackAxisSpacing * 2;
	private double sizeSel = trackAxisSpacing*trackAxisSpacing*5;

	private Collection<?> serviceAreas = null;
	public Collection<?> getServiceAreas() { return serviceAreas; }

	private Collection<?> doubleTrackAreas = null;
	public Collection<?> getDoubleTrackAreas() { return doubleTrackAreas; }


	public RailwayAreasDetection(Collection<Feature> secs) {
		this.secs = secs;
	}


	public void compute() {

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
		serviceAreas = JTSGeomUtil.getPolygonGeometries(servArea, sizeSel);
		servArea = null;
		doubleTrackAreas = JTSGeomUtil.getPolygonGeometries(doubleTrack, sizeSel);
		doubleTrack = null;

		LOGGER.info("   nbAreas = " + serviceAreas.size() + "   nbDoubleTracks = " + doubleTrackAreas.size());
	}

}
