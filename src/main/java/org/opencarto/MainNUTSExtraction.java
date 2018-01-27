package org.opencarto;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.imageio.ImageIO;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.swing.JMapFrame;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CompressUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.mapping.MappingUtils;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MainNUTSExtraction {

	public static void main(String[] args) {
		String outPath = "/home/juju/Bureau/drafts/cnts/";

		//load nuts regions
		ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT.shp", 4258).fs; //4258 4326
		ArrayList<Feature> fsLAEA = SHPUtil.loadSHP("/home/juju/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT_LAEA.shp", 3035).fs;

		//extract all cnt ids
		HashSet<String> cnts = new HashSet<String>();
		for(Feature f : fs) cnts.add(f.getProperties().get("CNTR_ID").toString());


		for(String cnt : cnts) {
		//for(String cnt : new String[] { "BE"/*"FR","BE","DE"*/}) {

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
			makeMap(outPath, cnt);


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

			//TODO make and save all other levels? make boundaries?
		}

		System.out.println("End");
	}


	private static void makeMap(String o, String cnt) {
		//make overview image
		SimpleFeatureCollection sfc = SHPUtil.getSimpleFeatures(o + "NUTS_RG_2016_01M_DRAFT_"+cnt+"_LAEA.shp");

		MapContent map = new MapContent();
		CoordinateReferenceSystem crs = sfc.getSchema().getCoordinateReferenceSystem();
		map.getViewport().setCoordinateReferenceSystem(crs);
		map.getViewport().setBounds(sfc.getBounds());
		map.setTitle(cnt+" - NUTS 3");



		//add layer for no data
		map.addLayer( new FeatureLayer(sfc, MappingUtils.getPolygonStyle(Color.LIGHT_GRAY, Color.BLACK, 0.3)) );
		map.addLayer( new FeatureLayer(sfc, MappingUtils.getTextStyle("NUTS3",12)) );

		//JMapFrame.showMap(map);

		BufferedImage image = MappingUtils.getImage(map, 1000, Color.WHITE);
		map.dispose();
		try { ImageIO.write(image, "png", new File(o+"NUTS_RG_2016_01M_DRAFT_"+cnt+".png")); } catch (IOException e) { e.printStackTrace(); }
	}

}
