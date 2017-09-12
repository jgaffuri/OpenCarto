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
		new File(outPath).mkdirs();

		for(int code=10; code<=20; code++){
			shpToCSV(shpFileFolder + "itai"+code+"___________pi", outPath + "ita"+code+".csv");
		}

	}

	private static void shpToCSV(String inSHP, String outCSV) throws Exception{
		LOGGER.info("Load "+inSHP);
		ArrayList<Feature> fs = SHPUtil.loadSHP(inSHP).fs;

		LOGGER.info("Prepare file");
		File file = new File(outCSV);
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
				Object o = f.getProperties().get(key);
				bw.write(o==null?"":o.toString().replaceAll(",", ";"));
				if(i<keys.size()-1) bw.write(","); i++;
			}
			bw.write(",");
			bw.write(f.getGeom().toText());
			bw.write("\n");
		}

		bw.close();

	}
}
