package org.opencarto;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

public class MainGISCOGeometryNoding {

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath + "out/100k_1M/comm/out_narrow_gaps_removed.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("ADM0_CODE") != null) f.id = ""+f.getProperties().get("ADM0_CODE");
			else if(f.getProperties().get("ADM0_NAME") != null) f.id = ""+f.getProperties().get("ADM_NAME");

		//build spatial index for units
		SpatialIndex index = new STRtree();
		for(Feature f : fs) index.insert(f.getGeom().getEnvelopeInternal(), f);

		




		System.out.println("End");
	}

}
