/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationQuality;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;
import org.opencarto.util.ProjectionUtil.CRSType;

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

		int maxCoordinatesNumber = 50000;
		int objMaxCoordinateNumber = 15000;


		LOGGER.info("Load data from "+in);
		Collection<Feature> units = SHPUtil.loadSHP(in).fs;
		LOGGER.info("Set ID");
		for(Feature f : units) f.id = ""+f.get(idCol);


		LOGGER.info("Check identifier");
		FeatureUtil.checkIdentfier(units, idCol);
		LOGGER.info("Check quality");
		TesselationQuality.checkQuality(units, 1e-6, basePath + "qc.csv", true, maxCoordinatesNumber, objMaxCoordinateNumber, false);







		LOGGER.info("Fix quality");
		double eps = 1e-9;
		units = TesselationQuality.fixQuality(units, new Envelope(-180+eps, 180-eps, -90+eps, 90-eps), 1e-7, maxCoordinatesNumber, objMaxCoordinateNumber, false);

		LOGGER.info("Save output data in "+out);
		for(Feature f : units) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(units, basePath+"GLOBAL_ADMIN_AREAS_clean.shp", SHPUtil.getCRS(in));



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
