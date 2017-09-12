package org.opencarto;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CSVUtil;
import org.opencarto.io.SHPUtil;

public class SHPPointToCSV {
	private final static Logger LOGGER = Logger.getLogger(SHPPointToCSV.class);

	public static void main(String[] args) {


		XXXX

	}

	private void export(String shpFileFolder, String shpFile, int epsg, String outPath){
		LOGGER.info("Load "+shpFile);
		ArrayList<Feature> fs = SHPUtil.loadSHP(shpFileFolder+shpFile+".shp",epsg).fs;

		LOGGER.info("Build CSV data");
		ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		for(Feature f : fs) {
			Map<String, Object> p = f.getProperties();
			p.put("geom", f.getGeom().toText()); f.setGeom(null);
			data.add(p);
		}

		LOGGER.info("Save as CSV");
		CSVUtil.save(data, outPath, shpFile+".csv");


	}
}
