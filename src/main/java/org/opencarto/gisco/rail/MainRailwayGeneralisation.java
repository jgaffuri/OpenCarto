/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.geotools.filter.text.cql2.CQL;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.util.FeatureUtil;
import org.opengis.filter.Filter;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayGeneralisation {
	public final static Logger LOGGER = Logger.getLogger(MainRailwayGeneralisation.class.getName());

	public static void main(String[] args) throws Exception {

		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";
		Filter fil = CQL.toFilter( "CNTR = 'NL'" );
		ArrayList<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;
		LOGGER.info(secs.size()+"   "+FeatureUtil.getVerticesNumber(secs));


		//get partition
		Collection<Feature> parts = Partition.getPartitionDataset(secs, 10000, 1000);
		SHPUtil.saveSHP(parts, basePath+"out/partition.shp", SHPUtil.getCRS(inFile));


		/*
		//compute areas
		RailwayServiceAreasDetection rsad = new RailwayServiceAreasDetection(secs);
		rsad.compute();

		LOGGER.info("Save");
		SHPUtil.saveGeomsSHP((Collection<Geometry>) rsad.getServiceAreas(), basePath+"out/service_areas.shp", SHPUtil.getCRS(inFile));
		SHPUtil.saveGeomsSHP((Collection<Geometry>) rsad.getDoubleTrackAreas(), basePath+"out/double_tracks_areas.shp", SHPUtil.getCRS(inFile));
		 */

		System.out.println("End");
	}

}
