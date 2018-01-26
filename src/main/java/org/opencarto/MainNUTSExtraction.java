package org.opencarto;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CompressUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.mapping.StatisticalMap;

public class MainNUTSExtraction {

	public static void main(String[] args) {
		String outPath = "/home/juju/Bureau/drafts/cnts/";

		//load nuts regions
		ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT.shp", 4258).fs;

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

			//save as new shp file
			SHPUtil.saveSHP(fs_, o, "NUTS_RG_2016_01M_DRAFT_"+cnt+".shp");


			//make overview image
			SimpleFeatureCollection sfc = SHPUtil.getSimpleFeatures(o + "NUTS_RG_2016_01M_DRAFT_"+cnt+".shp");
			StatisticalMap map = new StatisticalMap(sfc, "NUTS3", null, null, null);
			map.setTitle(cnt+" - NUTS 3");
			map.setNoDataColor(Color.LIGHT_GRAY);
			//map.setBounds(x1, x2, y1, y2);
			map.make();
			map.saveAsImage(o+"NUTS_RG_2016_01M_DRAFT_"+cnt+".png", 2000, true, false);
			map.dispose();

			//zip everything
			//TODO zip folder - automatic
			CompressUtil.createZIP(outPath+"NUTS_RG_2016_01M_DRAFT_"+cnt+".zip", o, new String[] {
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".dbf",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".fix",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".prj",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".shp",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".shx",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".png"
			});



			//make and save all other levels? make boundaries?
		}

		System.out.println("End");
	}

}
