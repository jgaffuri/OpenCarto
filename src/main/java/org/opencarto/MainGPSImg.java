package org.opencarto;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.MultiScaleProperty;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.style.ColorScale;
import org.opencarto.style.Style;
import org.opencarto.style.basic.LineStyle;
import org.opencarto.style.gps.GPSSpeedStyle;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.raster.RasterTileBuilder;

public class MainGPSImg {

	public static void main(String[] args) {
		//String[] inPaths = new String[] {"/home/juju/GPS/strava/","/home/juju/GPS/gpx/"};
		String[] inPaths = new String[] {"/home/juju/GPS/gpx_test/"};
		String outPath = "/home/juju/GPS/app_raster/gps_traces_raster/";
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
		//MultiScaleProperty<Style<GPSTrace>> styleSpeed = new MultiScaleProperty<Style<GPSTrace>>()

		MultiScaleProperty<Style<GPSTrace>> style = new MultiScaleProperty<Style<GPSTrace>>()
				.set(new LineStyle<GPSTrace>().setWidth(1.3f).setColor(Color.BLUE), 0, 8)
				.set(new LineStyle<GPSTrace>().setWidth(1f).setColor(Color.BLUE), 9, 11)
				.set(new LineStyle<GPSTrace>().setWidth(0.7f).setColor(Color.BLUE), 12, 20)
				;
		new Tiling(fs, new RasterTileBuilder<GPSTrace>(style), outPath + "default/", zs, false).doTiling();

		//make tiles - by speed
		ColorScale col = new ColorScale(){
			public Color getColor(double value) {
				return Color.RED;
			}
		};
		MultiScaleProperty<Style<GPSTrace>> styleSpeed = new MultiScaleProperty<Style<GPSTrace>>()
				.set(new GPSSpeedStyle(col).setWidth(1.3f), 0, 8)
				.set(new GPSSpeedStyle(col).setWidth(1f), 9, 11)
				.set(new GPSSpeedStyle(col).setWidth(0.7f), 12, 20)
				;
		//new Tiling(fs, new RasterTileBuilder(styleSpeed), outPath + "speed/", zs, false).doTiling();

		//make tiles - by date
		//TODO

		System.out.println("Done.");
	}

}
