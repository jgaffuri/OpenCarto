package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationQuality;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Envelope;

public class MainGenFixInput {
	private final static Logger LOGGER = Logger.getLogger(MainGenFixInput.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		LOGGER.info("Load data");
		int epsg = 4258; Collection<Feature> units = SHPUtil.loadSHP(basePath+"nutsplus/NUTS_PLUS_01M_1403.shp", epsg).fs;
		//int epsg = 4258; Collection<Feature> fs = SHPUtil.loadSHP(basePath+"test/testQ.shp", epsg).fs;
		for(Feature f : units) f.id = ""+f.getProperties().get("NUTS_P_ID");

		LOGGER.info("Fix quality");
		double eps = 1e-9;
		units = TesselationQuality.fixQuality(units, new Envelope(-180+eps, 180-eps, -90+eps, 90-eps), 1e-7, 3000000, 15000);

		LOGGER.info("Save");
		for(Feature f : units) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(units, basePath+"nutsplus/", "NUTS_PLUS_01M_1403_clean.shp");
		//SHPUtil.saveSHP(fs, basePath+"test/", "testQ_clean.shp");

		System.out.println("End");
	}

}
