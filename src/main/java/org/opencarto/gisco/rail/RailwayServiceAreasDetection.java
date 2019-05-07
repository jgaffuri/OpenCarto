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
public class RailwayServiceAreasDetection {

	public final static Logger LOGGER = Logger.getLogger(RailwayServiceAreasDetection.class.getName());

	private Collection<Feature> secs;

	private int quad = 5;
	private double bufferDistance = 12;
	private double shrinkingDistance = 3;
	private double sizeSel = bufferDistance*bufferDistance * 3;

	private Collection<?> serviceAreas = null;
	public Collection<?> getServiceAreas() { return serviceAreas; }

	private Collection<?> doubleTrackAreas = null;
	public Collection<?> getDoubleTrackAreas() { return doubleTrackAreas; }


	public RailwayServiceAreasDetection(Collection<Feature> secs) {
		this.secs = secs;
	}


	public void compute() {

		LOGGER.info("Buffer-union of all geometries");
		Geometry g = new BufferAggregation(bufferDistance, -1, quad, -1, false).aggregateGeometries(FeatureUtil.getGeometries(secs));
		LOGGER.info("Buffer in");
		g = g.buffer(-bufferDistance);

		LOGGER.info("Buffers for service areas");
		Geometry servArea = g.buffer(-shrinkingDistance, quad).buffer(shrinkingDistance*1.001, quad);
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
