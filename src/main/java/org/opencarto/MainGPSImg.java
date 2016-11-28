package org.opencarto;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.MultiScaleProperty;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.style.basic.LineStyle;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.raster.RasterTileBuilder;

import com.vividsolutions.jts.geom.Geometry;

public class MainGPSImg {

	public static void main(String[] args) {
		String[] inPaths = new String[] {"/home/juju/GPS/strava/","/home/juju/GPS/gpx/"};
		//String[] inPaths = new String[] {"/home/juju/GPS/gpx_test/"};
		String outPath = "/home/juju/Bureau/gps_traces_raster";
		int zoomMax = 14;

		ZoomExtend zs = new ZoomExtend(0,zoomMax);

		//load traces
		ArrayList<GPSTrace> fs = new ArrayList<GPSTrace>();
		for(String inPath : inPaths){
			System.out.println("Load traces in "+inPath);
			File[] files = new File(inPath).listFiles();
			fs.addAll( GPSUtil.load(files) );
		}
		System.out.println(fs.size() + " traces loaded.");

		//make generalisation
		//new NoGeneralisation<GPSTrace>().perform(fs, zs);
		new DefaultGeneralisation<GPSTrace>(false).perform(fs, zs);

		//make tiles - default
		System.out.println("Tiling");
		MultiScaleProperty<Style> style = new MultiScaleProperty<Style>()
				.set(new LineStyle().setWidth(1.3f).setColor(Color.BLUE), 0, 8)
				.set(new LineStyle().setWidth(1f).setColor(Color.BLUE), 9, 11)
				.set(new LineStyle().setWidth(0.7f).setColor(Color.BLUE), 12, 20)
				;
		new Tiling(fs, new RasterTileBuilder(style), outPath + "default/", zs, false).doTiling();

		//make tiles - by speed
		//TODO get/define min/max speed
		MultiScaleProperty<Style> styleSpeed = new MultiScaleProperty<Style>(new Style(){
			@Override
			public void draw(Geometry geom, PointTransformation pt, Graphics2D gr) {
				//TODO
				//get trace speed
				//get corresponding color
				//draw
			}});
		//new Tiling(fs, new RasterTileBuilder(styleSpeed), outPath + "speed/", zs, false).doTiling();

		//make tiles - by date
		//TODO get/define min/max date
		MultiScaleProperty<Style> styleDate = new MultiScaleProperty<Style>(new Style(){
			@Override
			public void draw(Geometry geom, PointTransformation pt, Graphics2D gr) {
				//TODO
			}});
		//new Tiling(fs, new RasterTileBuilder(styleDate), outPath + "date/", zs, false).doTiling();

		System.out.println("Done.");
	}

}
