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
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.ProjectionUtil.CRSType;

/**
 * @author julien Gaffuri
 *
 */
public class MainGen {
	private final static Logger LOGGER = Logger.getLogger(MainGen.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/sett/";
		String in = basePath+"SETTLEMENT_A_100K.shp";
		String pattern = "SETTLEMENT_A_";
		int roundNb = 5;
		int maxCoordinatesNumber = 50000;
		int objMaxCoordinateNumber = 50000;

		CRSType crsType = SHPUtil.getCRSType(in);

		//LOGGER.info("Check quality");
		//TesselationQuality.checkQuality(SHPUtil.loadSHP(in).fs, 1e-6, basePath + "qc.csv", true, maxCoordinatesNumber, objMaxCoordinateNumber, true);
		//LOGGER.info("Check identifier");
		//FeatureUtil.checkIdentfier(SHPUtil.loadSHP(in).fs, "ID");

		//for(double s : new double[]{1,3,10,20,60}) {
		for(double s : new double[]{1}) {
			double scaleDenominator = s*1e6;

			LOGGER.info("Load data for "+((int)s)+"M generalisation");
			Collection<Feature> units = SHPUtil.loadSHP(in).fs;

			LOGGER.info("Fix quality");
			units = TesselationQuality.fixQuality(units, null, 1e-7, maxCoordinatesNumber, objMaxCoordinateNumber);

			LOGGER.info("Launch generalisation for "+((int)s)+"M");
			units = TesselationGeneralisation.runGeneralisation(units, null, crsType, scaleDenominator, roundNb, maxCoordinatesNumber, objMaxCoordinateNumber);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath + pattern+((int)s)+"M.shp", SHPUtil.getCRS(in));
		}

		LOGGER.info("End");
	}

}
