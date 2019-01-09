/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationQuality;
import org.opencarto.util.ProjectionUtil.CRSType;

/**
 * @author julien Gaffuri
 *
 */
public class MainGen {
	private final static Logger LOGGER = Logger.getLogger(MainGen.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		String in = "/home/juju/Bureau/nuts_gene_data/sett/SETTLEMENT_A.shp";
		String out = "/home/juju/Bureau/nuts_gene_data/sett/SETTLEMENT_A_1M.shp";
		int roundNb = 5;
		int maxCoordinatesNumber = 50000;
		int objMaxCoordinateNumber = 15000;

		CRSType crsType = SHPUtil.getCRSType(in);

		//LOGGER.info("Check quality");
		//TesselationQuality.checkQuality(SHPUtil.loadSHP(in).fs, 1e-6, basePath + "qc.csv", true, maxCoordinatesNumber, objMaxCoordinateNumber, false);
		//LOGGER.info("Check identifier");
		//FeatureUtil.checkIdentfier(SHPUtil.loadSHP(in).fs, "ID");

		double scaleDenominator = 1.0*1e6;

		LOGGER.info("Load data from "+in);
		Collection<Feature> units = SHPUtil.loadSHP(in).fs;

		LOGGER.info("Fix quality");
		units = TesselationQuality.fixQuality(units, null, 1e-7, maxCoordinatesNumber, objMaxCoordinateNumber, false);

		LOGGER.info("Launch generalisation");
		units = TesselationGeneralisation.runGeneralisation(units, null, crsType, scaleDenominator, roundNb, maxCoordinatesNumber, objMaxCoordinateNumber);

		LOGGER.info("Save output data in "+out);
		SHPUtil.saveSHP(units, out, SHPUtil.getCRS(in));

		LOGGER.info("End");
	}

}
