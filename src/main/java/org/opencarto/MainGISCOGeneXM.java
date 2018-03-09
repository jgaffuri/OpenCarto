/**
 * 
 */
package org.opencarto;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOGeneXM {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeneXM.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";

		for(double s : new double[]{1,3,10,20,60}) {
			double scaleDenominator = s*1e6;

			LOGGER.info("Load data");
			final int epsg = 3857; final String rep="serbia"; String inFile = basePath+"serbia/NUTS_3_serbia_WM.shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
			for(Feature f : units) for(String id : new String[] {"NUTS_CODE","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

			LOGGER.info("Launch generalisation");
			units = DefaultTesselationGeneralisation.runGeneralisation(units, DefaultTesselationGeneralisation.defaultSpecs, scaleDenominator, 4, false);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, outPath+ rep+"/", "NUTS_3_serbia_"+(int)s+"M_WM.shp");
		}

		LOGGER.info("End");
	}

}
