/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;

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

		//String file = "URAU_02_2018_T_LAEA";
		//String file = "URAU_02_2018_K_LAEA";
		//String file = "URAU_02_2018_F_LAEA";
		//String file = "URAU_02_2018_C_LAEA";
		//String file = "URAU_02_2018_C_F_LAEA";
		for(String file : new String[] {"URAU_F_0504_LAEA","URAU_K_0504_LAEA","URAU_C_0504_LAEA"})
		{
			LOGGER.info("Load data "+file);
			int epsg = 3035; String inFile = path+file+".shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;

			LOGGER.info("Launch generalisation");
			units = TesselationGeneralisation.runGeneralisation(units, null, TesselationGeneralisation.defaultSpecs, 1e6, 10, false, 1000000, 1000);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, path, file+"_1M.shp");
		}

		LOGGER.info("End");
	}

}
