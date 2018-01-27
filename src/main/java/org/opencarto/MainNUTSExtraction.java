package org.opencarto;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CompressUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.mapping.MappingUtils;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MainNUTSExtraction {

	public static void main(String[] args) {
		System.setProperty("org.geotools.referencing.forceXY", "true");

		String outPath = "/home/juju/Bureau/drafts/cnts/";

		//load nuts regions
		ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT.shp", 4258).fs; //4258 4326
		ArrayList<Feature> fsLAEA = SHPUtil.loadSHP("/home/juju/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT_LAEA.shp", 3035).fs;

		//extract all cnt ids
		HashSet<String> cnts = new HashSet<String>();
		for(Feature f : fs) cnts.add(f.getProperties().get("CNTR_ID").toString());


		for(String cnt : cnts) {
			//for(String cnt : new String[] { "DE","SE","LU" }) {

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




			//filter - nuts 3 regions for cnt
			ArrayList<Feature> fsLAEA_ = new ArrayList<Feature>();
			for(Feature f : fsLAEA)
				if(f.getProperties().get("CNTR_ID").equals(cnt))
					fsLAEA_.add(f);

			//save as new shp file
			SHPUtil.saveSHP(fsLAEA_, o, "NUTS_RG_2016_01M_DRAFT_"+cnt+"_LAEA.shp");



			//make map image
			SimpleFeatureCollection sfc = SHPUtil.getSimpleFeatures(o + "NUTS_RG_2016_01M_DRAFT_"+cnt+"_LAEA.shp");
			if(cnt.equals("FR")) {
				//TODO
			} else {
				makeMap(sfc, outPath, cnt, sfc.getBounds());
			}

			//zip everything
			CompressUtil.createZIP(outPath+"NUTS_RG_2016_01M_DRAFT_"+cnt+".zip", o, new String[] {
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".dbf",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".fix",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".prj",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".shp",
					"NUTS_RG_2016_01M_DRAFT_"+cnt+".shx",
					//"NUTS_RG_2016_01M_DRAFT_"+cnt+".png"
			});

		}

		System.out.println("End");
	}


	//make overview image
	private static void makeMap(SimpleFeatureCollection sfc, String outPath, String fileCodeName, ReferencedEnvelope bounds) {

		MapContent map = new MapContent();
		CoordinateReferenceSystem crs = sfc.getSchema().getCoordinateReferenceSystem();
		map.getViewport().setCoordinateReferenceSystem(crs);
		map.getViewport().setBounds(bounds);
		map.setTitle(fileCodeName+" - NUTS 3");

		//add layer for no data
		map.addLayer( new FeatureLayer(sfc, MappingUtils.getPolygonStyle(Color.LIGHT_GRAY, Color.BLACK, 0.3)) );
		map.addLayer( new FeatureLayer(sfc, MappingUtils.getTextStyle("NUTS3",12)) );


		//build image
		MappingUtils.saveAsImage(map, 1e6, Color.WHITE, outPath, "overview_"+fileCodeName+".png");

		//JMapFrame.showMap(map);
		map.dispose();
	}

}
