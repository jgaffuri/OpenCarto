package org.opencarto;

import java.util.ArrayList;

import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

public class MainGISCOGeometryFixNoding {


	public static void main(String[] args) {
		System.out.println("Start");

		System.out.println("Load data");
		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath + "out/100k_1M/comm/out_narrow_gaps_removed.shp", epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath + "out/100k_1M/comm/out_narrow_gaps_removed_noded.shp", epsg).fs;

		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("ADM0_CODE") != null) f.id = ""+f.getProperties().get("ADM0_CODE");
			else if(f.getProperties().get("ADM0_NAME") != null) f.id = ""+f.getProperties().get("ADM_NAME");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");
			else if(f.getProperties().get("GISCO_ID") != null) f.id = ""+f.getProperties().get("GISCO_ID");

		//fix noding issue
		double nodingResolution = 1e-5;
		NodingUtil.fixNoding(fs, nodingResolution);

		//save output
		SHPUtil.saveSHP(fs, basePath+ "out/100k_1M/comm/", "out_narrow_gaps_removed_noded.shp");

		System.out.println("End");
	}

}
