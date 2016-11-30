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
		String[] inPaths = new String[] {"/home/juju/GPS/strava/","/home/juju/GPS/gpx/"};
		//String[] inPaths = new String[] {"/home/juju/GPS/gpx_test/"};
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
		System.out.println("Tiling default");
		//MultiScaleProperty<Style<GPSTrace>> styleSpeed = new MultiScaleProperty<Style<GPSTrace>>()

		MultiScaleProperty<Style<GPSTrace>> style = new MultiScaleProperty<Style<GPSTrace>>()
				.set(new LineStyle<GPSTrace>().setWidth(1f).setColor(Color.BLUE), 0, 8)
				.set(new LineStyle<GPSTrace>().setWidth(0.8f).setColor(Color.BLUE), 9, 11)
				.set(new LineStyle<GPSTrace>().setWidth(0.6f).setColor(Color.BLUE), 12, 20)
				;
		new Tiling(fs, new RasterTileBuilder<GPSTrace>(style), outPath + "default/", zs, false).doTiling();

		//make tiles - by speed
		System.out.println("Tiling speed");
		ColorScale col = new ColorScale(){
			public Color getColor(double value) {
				//TODO something continuous?
				if(value<=0) return Color.CYAN;
				if(value<6) return Color.CYAN;
				else if(value<13) return Color.BLUE;
				else if(value<30) return Color.MAGENTA;
				else if(value<120) return Color.RED;
				else if(value<200) return Color.ORANGE;
				return Color.YELLOW;
			}
		};
		MultiScaleProperty<Style<GPSTrace>> styleSpeed = new MultiScaleProperty<Style<GPSTrace>>()
				.set(new GPSSpeedStyle(col, 1f), 0, 8)
				.set(new GPSSpeedStyle(col, 0.9f), 9, 11)
				.set(new GPSSpeedStyle(col, 0.8f), 12, 20)
				;
		new Tiling(fs, new RasterTileBuilder<GPSTrace>(styleSpeed), outPath + "speed/", zs, false).doTiling();

		/*
		Impossible to parse date: 2016-01-17T14:26:54.610Z
Impossible to parse date: 2016-01-16T11:22:19.750Z
Impossible to parse date: 2016-01-17T09:26:46.830Z
Impossible to parse date: 2016-01-17T15:51:49.650Z
Impossible to parse date: 2016-01-15T12:11:22.560Z
Impossible to parse date: 2016-01-16T13:15:43.610Z
Impossible to parse date: 2016-01-16T15:56:16.650Z
		*/

		//make tiles - by date
		//TODO

		//make tiles - by type
		//TODO

		System.out.println("Done.");
	}

}
