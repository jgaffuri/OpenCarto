package org.opencarto;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

public class SHPPointToCSV {
	private final static Logger LOGGER = Logger.getLogger(SHPPointToCSV.class);

	public static void main(String[] args) throws Exception {

		String shpFileFolder = "H:/desktop/ita/";
		String outPath = "H:/desktop/ita_csv/";

		for(int code=10; code<=20; code++){
			export(shpFileFolder, "itai"+code+"___________pi", 4326, outPath);
		}

	}

	private static void export(String shpFileFolder, String shpFile, int epsg, String outPath) throws Exception{
		LOGGER.info("Load "+shpFile);
		ArrayList<Feature> fs = SHPUtil.loadSHP(shpFileFolder+shpFile+".shp",epsg).fs;

		LOGGER.info("Prepare file");
		new File(outPath).mkdirs();
		File file = new File(outPath+shpFile+".csv");
		if(file.exists()) file.delete();
		file.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));

		LOGGER.info("Write header");
		ArrayList<String> keys = new ArrayList<String>(fs.get(0).getProperties().keySet());
		int i=0;
		for(String key : keys ){
			bw.write(key.replaceAll(",", ";"));
			if(i<keys.size()-1) bw.write(","); i++;
		}
		bw.write(",geom\n");

		LOGGER.info("Write data");
		for(Feature f : fs) {
			i=0;
			for(String key : keys){
				bw.write(f.getProperties().get(key).toString().replaceAll(",", ";"));
				if(i<keys.size()-1) bw.write(","); i++;
			}
			bw.write(",");
			bw.write(f.getGeom().toText());
			bw.write("\n");
		}

		bw.close();

	}
}
