package org.opencarto;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.JTSGeomUtil;

public class MainGISCOGeometryFixInput {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeometryFixInput.class.getName());


	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";
		Collection<Feature> fs;

		LOGGER.info("Load data");
		final int epsg = 3857; final String rep="100k_1M/commplus"; fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_FINAL.shp", epsg).fs;
		//final int epsg = 3857; final String rep="100k_1M/commplus"; fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/out_narrow_gaps_removed___.shp", epsg).fs;

		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");
			else if(f.getProperties().get("GISCO_ID") != null) f.id = ""+f.getProperties().get("GISCO_ID");

		//make valid

		//fix noding issue
		//double nodingResolution = 1e-5;
		//NodingUtil.fixNoding(fs, nodingResolution);

		LOGGER.info("Save");
		for(Feature f : fs) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs, outPath+ rep+"/", "noded.shp");

		System.out.println("End");
	}

}
