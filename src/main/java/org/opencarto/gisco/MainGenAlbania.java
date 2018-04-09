/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;
import org.opencarto.util.ProjectionUtil.CRSType;

/**
 * @author julien Gaffuri
 *
 */
public class MainGenAlbania {
	private final static Logger LOGGER = Logger.getLogger(MainGenAlbania.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/alb/";

		//TesselationQuality.checkQuality(SHPUtil.loadSHP(basePath+"/SU_AL_100k.shp", 4258).fs, 1e-6, basePath + "qc.csv", true, 30000000, 150000);

		for(double s : new double[]{1,3,10,20,60}) {
			double scaleDenominator = s*1e6;

			LOGGER.info("Load data for "+((int)s)+"M generalisation");
			Collection<Feature> units = SHPUtil.loadSHP(basePath+"/SU_AL_100k_.shp", 4258).fs;

			//LOGGER.info("Fix quality");
			//units = TesselationQuality.fixQuality(units, null, 1e-7, 30000000, 150000);

			LOGGER.info("Launch generalisation for "+((int)s)+"M");
			int roundNb = 5;
			units = TesselationGeneralisation.runGeneralisation(units, null, CRSType.GEOG, null, scaleDenominator, roundNb, false, 10000000, 10000);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath + "/SU_AL_"+((int)s)+"M.shp");
		}

		LOGGER.info("End");
	}

}
