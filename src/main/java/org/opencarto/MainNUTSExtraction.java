package org.opencarto;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.imageio.ImageIO;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.CompressUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.mapping.MappingUtils;
import org.opencarto.util.ProjectionUtil;
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
			if(!cnt.equals("FR"))
				makeMap(o + "NUTS_RG_2016_01M_DRAFT_"+cnt+"_LAEA.shp", outPath, cnt);



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


	private static void makeMap(String inputFile, String outPath, String cnt) {

		//make overview image
		SimpleFeatureCollection sfc = SHPUtil.getSimpleFeatures(inputFile);

		MapContent map = new MapContent();
		CoordinateReferenceSystem crs = sfc.getSchema().getCoordinateReferenceSystem();
		map.getViewport().setCoordinateReferenceSystem(crs);
		map.getViewport().setBounds(sfc.getBounds());
		map.setTitle(cnt+" - NUTS 3");

		//add layer for no data
		map.addLayer( new FeatureLayer(sfc, MappingUtils.getPolygonStyle(Color.LIGHT_GRAY, Color.BLACK, 0.3)) );
		map.addLayer( new FeatureLayer(sfc, MappingUtils.getTextStyle("NUTS3",12)) );

		Color imgBckgrdColor = Color.WHITE;


		//compute image dimensions
		ReferencedEnvelope mapBounds = map.getViewport().getBounds();
		double scaleDenom = 1e6;
		int imageWidth = (int) (mapBounds.getWidth() / scaleDenom / ProjectionUtil.METERS_PER_PIXEL +1);
		int imageHeight = (int) (mapBounds.getHeight() / scaleDenom / ProjectionUtil.METERS_PER_PIXEL +1);
		/*int imageWidth = 1000;
		int imageHeight = (int) Math.round(imageWidth * mapBounds.getSpan(1) / mapBounds.getSpan(0));*/

		//build renderer
		StreamingRenderer renderer = new StreamingRenderer();
		renderer.setMapContent(map);
		renderer.setGeneralizationDistance(-1);
		renderer.setJava2DHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ));
		Map<Object,Object> renderingHints = new HashMap<Object,Object>();
		renderingHints.put("optimizedDataLoadingEnabled", Boolean.TRUE);
		renderer.setRendererHints( renderingHints );

		//draw image with renderer
		Rectangle imageBounds = new Rectangle(0, 0, imageWidth, imageHeight);
		BufferedImage image = new BufferedImage(imageWidth, imageBounds.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gr = image.createGraphics();
		gr.setPaint(imgBckgrdColor);
		gr.fill(imageBounds);
		renderer.paint(gr, imageBounds, mapBounds /*,new AffineTransform(0,1, 1,0, 0,0)*/);

		//JMapFrame.showMap(map);
		map.dispose();
		try { ImageIO.write(image, "png", new File(outPath+"overview_"+cnt+".png")); } catch (IOException e) { e.printStackTrace(); }
	}

}
