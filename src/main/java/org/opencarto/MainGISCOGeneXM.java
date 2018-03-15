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

		for(double s : new double[]{3,10,20,60}) {
			double scaleDenominator = s*1e6;

			LOGGER.info("Load data for "+((int)s)+"M");
			final int epsg = 3857; String inFile = basePath+"nutsplus/NUTS_PLUS_01M_1403_WM.shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
			for(Feature f : units) for(String id : new String[] {"NUTS_P_ID","NUTS_CODE","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

			for(int i=1; i<=8; i++) {
				LOGGER.info("Launch generalisation " + i + " for "+((int)s)+"M");
				units = DefaultTesselationGeneralisation.runGeneralisation(units, DefaultTesselationGeneralisation.defaultSpecs, scaleDenominator, 1, false);

				LOGGER.info("Run GC");
				System.gc();

				LOGGER.info("Save output data");
				SHPUtil.saveSHP(units, basePath + "out/nutsplus/", "NUTS_PLUS_"+((int)s)+"M_WM_"+i+".shp");
			}

			//LOGGER.info("Launch generalisation for "+((int)s)+"M");
			//units = DefaultTesselationGeneralisation.runGeneralisation(units, DefaultTesselationGeneralisation.defaultSpecs, scaleDenominator, 5, false);

			//LOGGER.info("Save output data for "+((int)s)+"M");
			//SHPUtil.saveSHP(units, basePath + "out/nutsplus/", "NUTS_PLUS_"+((int)s)+"M_WM.shp");
		}

		LOGGER.info("End");
	}

}
