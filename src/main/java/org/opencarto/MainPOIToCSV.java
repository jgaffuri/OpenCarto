package org.opencarto;
import java.io.File;

import org.opencarto.io.SHPUtil;

public class MainPOIToCSV {

	public static void main(String[] args) throws Exception {

		String shpFileFolder = "H:/desktop/ita/";
		String outPath = "H:/desktop/ita_csv/";
		new File(outPath).mkdirs();

		for(int code=10; code<=20; code++)
			SHPUtil.shpToCSV(shpFileFolder + "itai"+code+"___________pi.shp", outPath + "ita"+code+".csv");

	}

}
