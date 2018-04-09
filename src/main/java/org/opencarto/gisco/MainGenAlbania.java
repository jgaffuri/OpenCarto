/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.CartographicResolution.CRSType;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;

/**
 * @author julien Gaffuri
 *
 */
public class MainGenAlbania {
	private final static Logger LOGGER = Logger.getLogger(MainGenAlbania.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/alb/";

		for(double s : new double[]{1,3,10,20,60}) {
			double scaleDenominator = s*1e6;

			LOGGER.info("Load data for "+((int)s)+"M generalisation");
			Collection<Feature> units = SHPUtil.loadSHP(basePath+"/SU_AL_100k.shp", 4258).fs;
			for(Feature f : units) f.id = ""+f.getProperties().get("LVL2");

			LOGGER.info("Launch generalisation for "+((int)s)+"M");
			int roundNb = 10;
			units = TesselationGeneralisation.runGeneralisation(units, null, null, scaleDenominator, CRSType.GEOG, roundNb, false, 1000000, 1000);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath + "/SU_AL_"+((int)s)+"M.shp");
		}
		LOGGER.info("End");
	}

}
