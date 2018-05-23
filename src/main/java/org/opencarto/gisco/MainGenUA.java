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
public class MainGenUA {
	private final static Logger LOGGER = Logger.getLogger(MainGenUA.class.getName());
	//projs=("etrs89 4258" "wm 3857" "laea 3035")

	public static void main(String[] args) {
		LOGGER.info("Start");

		String path = "/home/juju/Bureau/ua_gene/";

		for(String file : new String[] {/*"URAU_K_0504_LAEA","URAU_C_0504_LAEA",*/"URAU_F_0504_LAEA"})
		{
			LOGGER.info("Load data "+file);
			String inFile = path+file+".shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile).fs;

			LOGGER.info("Launch quality fix and check");
			units = TesselationQuality.fixQuality(units, null, 1e-5, 3000000, 15000, false);
			//TesselationQuality.checkQuality(units, 1e-5, path + file+"_Q.csv", true, 3000000, 15000);

			LOGGER.info("Launch generalisation");
			TesselationGeneralisation.tracePartitioning = false;
			CRSType crsType = SHPUtil.getCRSType(inFile);
			units = TesselationGeneralisation.runGeneralisation(units, null, crsType, 1e6, 10, 1000000, 1000);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, path+file+"_1M.shp", SHPUtil.getCRS(inFile));
		}

		LOGGER.info("End");
	}

}
