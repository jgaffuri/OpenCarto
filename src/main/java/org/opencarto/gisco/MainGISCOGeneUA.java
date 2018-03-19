/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOGeneUA {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeneUA.class.getName());
	//projs=("etrs89 4258" "wm 3857" "laea 3035")

	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		for(String file : new String[] {"URAU_02_2018_C_F"}) {
			LOGGER.info("Load data "+file);
			int epsg = 3035; String inFile = basePath+"ua/"+file+".shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;

			LOGGER.info("Launch generalisation");
			units = DefaultTesselationGeneralisation.runGeneralisation(units, DefaultTesselationGeneralisation.defaultSpecs, 1e6, 10, false);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath+"out/ua/", file+"_1M.shp");
		}

		LOGGER.info("End");
	}

}
