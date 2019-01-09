/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationQuality;

import com.vividsolutions.jts.geom.Envelope;

/**
 * @author julien Gaffuri
 *
 */
public class MainGen {
	private final static Logger LOGGER = Logger.getLogger(MainGen.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/GLOBAL_ADMIN_AREAS/";
		String in = basePath+"GLOBAL_ADMIN_AREAS.shp";
		String out = basePath+"GLOBAL_ADMIN_AREAS_1M.shp";
		String idCol = "ID_";
		boolean tracePartitionning = true;

		int maxCoordinatesNumber = 1000000;
		int objMaxCoordinateNumber = 1000;



		//load data

		LOGGER.info("Load data from "+in);
		Collection<Feature> units = SHPUtil.loadSHP(in).fs;
		LOGGER.info("Set ID");
		for(Feature f : units) f.id = ""+f.get(idCol);


		//quality check

		//LOGGER.info("Check identifier");
		//FeatureUtil.checkIdentfier(units, idCol);
		//LOGGER.info("Check quality");
		//TesselationQuality.checkQuality(units, 1e-6, basePath + "qc.csv", true, maxCoordinatesNumber, objMaxCoordinateNumber, tracePartitionning);


		//quality correction

		LOGGER.info("Fix quality");
		double eps = 1e-9; Envelope env = new Envelope(-180+eps, 180-eps, -90+eps, 90-eps);
		units = TesselationQuality.fixQuality(units, env, 1e-7, maxCoordinatesNumber, objMaxCoordinateNumber, tracePartitionning);

		LOGGER.info("Save output data in "+out);
		SHPUtil.saveSHP(units, basePath+"GLOBAL_ADMIN_AREAS_clean.shp", SHPUtil.getCRS(in));


		//generalisation

		/*
		LOGGER.info("Launch generalisation");
		double scaleDenominator = 1.0*1e6;
		int roundNb = 5;
		CRSType crsType = SHPUtil.getCRSType(in);
		units = TesselationGeneralisation.runGeneralisation(units, null, crsType, scaleDenominator, roundNb, maxCoordinatesNumber, objMaxCoordinateNumber);

		LOGGER.info("Save output data in "+out);
		SHPUtil.saveSHP(units, out, SHPUtil.getCRS(in));
		 */


		LOGGER.info("End");
	}

}
