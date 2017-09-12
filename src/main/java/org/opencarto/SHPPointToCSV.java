package org.opencarto;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

	private void export(String shpFileFolder, String shpFile, int epsg, String outPath) throws Exception{
		LOGGER.info("Load "+shpFile);
		ArrayList<Feature> fs = SHPUtil.loadSHP(shpFileFolder+shpFile+".shp",epsg).fs;

		LOGGER.info("Prepare file");
		new File(outPath).mkdirs();
		File f = new File(outPath+shpFile+".csv");
		if(f.exists()) f.delete();
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));

		LOGGER.info("Write header");
		ArrayList<String> keys = new ArrayList<String>(fs.get(0).getProperties().keySet());
		int i=0;
		for(String key : keys ){
			bw.write(key);
			if(i<keys.size()-1) bw.write(",");
			i++;
		}
		bw.write(",geom\n");

		LOGGER.info("Write data");
		for(Feature f_ : fs) {
			i=0;
			for(String key : keys){
				bw.write(f_.getProperties().get(key).toString());
				if(i<keys.size()-1) bw.write(",");
				i++;
			}
			bw.write(",");
			bw.write(f_.getGeom().toText());
			bw.write("\n");
		}

		bw.close();

	}
}
