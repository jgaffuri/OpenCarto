package org.opencarto;

import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

public class MainNUTSExtraction {

	public static void main(String[] args) {
		String outPath = "/home/juju/Bureau/drafts/cnts/";

		//load nuts regions
		ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT.shp", 4258).fs;
		System.out.println(fs.size());

		//extract all cnt ids
		//CNTR_ID, NUTS3
		//for(Feature f : fs)

		for(String cnt : new String[] { "BE"/*"FR","BE","DE"*/}) {
			new File(outPath+cnt).mkdirs();

			//filter - nuts 3 regions for cnt
			ArrayList<Feature> fs_ = new ArrayList<Feature>();
			for(Feature f : fs)
				if(f.getProperties().get("CNTR_ID").equals(cnt))
					fs_.add(f);

			//save as new shp file
			SHPUtil.saveSHP(fs_, outPath+cnt, "NUTS_RG_2016_01M_DRAFT_"+cnt+".shp");

			//zip everything
			//make overview image

			//save as SHP
			//make and save all other levels? make boundaries?
		}

		System.out.println("End");
	}

}
