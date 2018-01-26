package org.opencarto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.CompressUtil;
import org.opencarto.io.SHPUtil;

public class MainNUTSExtraction {

	public static void main(String[] args) {
		String outPath = "/home/juju/Bureau/drafts/cnts/";

		//load nuts regions
		ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT.shp", 4258).fs;
		System.out.println(fs.size());

		//extract all cnt ids
		HashSet<String> cnts = new HashSet<String>();
		for(Feature f : fs) cnts.add(f.getProperties().get("CNTR_ID").toString());


		for(String cnt : new String[] { "BE"/*"FR","BE","DE"*/}) {
		//for(String cnt : cnts) {
			System.out.println(cnt);

			String o = outPath+cnt+"/";
			new File(o).mkdirs();

			//filter - nuts 3 regions for cnt
			ArrayList<Feature> fs_ = new ArrayList<Feature>();
			for(Feature f : fs)
				if(f.getProperties().get("CNTR_ID").equals(cnt))
					fs_.add(f);

			System.out.println("Nb="+fs_.size());

			//save as new shp file
			SHPUtil.saveSHP(fs_, o, "NUTS_RG_2016_01M_DRAFT_"+cnt+".shp");

			//zip everything
			CompressUtil.createZIP(outPath, cnt, new String[] {
					o+ "NUTS_RG_2016_01M_DRAFT_"+cnt+".shp",
					o+ "NUTS_RG_2016_01M_DRAFT_"+cnt+".prj"
			});

			
			//make overview image

			//save as SHP
			//make and save all other levels? make boundaries?
		}

		System.out.println("End");
	}

}
