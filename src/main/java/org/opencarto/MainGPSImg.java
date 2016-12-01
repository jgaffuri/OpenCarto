package org.opencarto;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.MultiScaleProperty;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSSegment;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.style.ColorScale;
import org.opencarto.style.Style;
import org.opencarto.style.gps.GPSSegmentSpeedStyle;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.raster.RasterTileBuilder;
import org.opencarto.util.ColorUtil;

public class MainGPSImg {

	public static void main(String[] args) {
		//String[] inPaths = new String[] {"/home/juju/GPS/strava/","/home/juju/GPS/gpx/"};
		String[] inPaths = new String[] {"/home/juju/GPS/strava/"};
		//String[] inPaths = new String[] {"/home/juju/GPS/gpx_test/"};
		String outPath = "/home/juju/GPS/app_raster/gps_traces_raster/";
		int zoomMax = 14;

		ZoomExtend zs = new ZoomExtend(0,zoomMax);

		//load traces
		ArrayList<GPSTrace> traces = new ArrayList<GPSTrace>();
		for(String inPath : inPaths){
			System.out.println("Load traces in "+inPath);
			File[] files = new File(inPath).listFiles();
			traces.addAll( GPSUtil.load(files) );
		}
		System.out.println(traces.size() + " traces loaded.");

		//make generalisation
		//new NoGeneralisation<GPSTrace>().perform(fs, zs);
		new DefaultGeneralisation<GPSTrace>(false).perform(traces, zs);




		/*/make tiles - default
		System.out.println("Tiling default");
		//MultiScaleProperty<Style<GPSTrace>> styleSpeed = new MultiScaleProperty<Style<GPSTrace>>()

		MultiScaleProperty<Style<GPSTrace>> style = new MultiScaleProperty<Style<GPSTrace>>()
				.set(new LineStyle<GPSTrace>().setWidth(1f).setColor(Color.BLUE), 0, 8)
				.set(new LineStyle<GPSTrace>().setWidth(0.8f).setColor(Color.BLUE), 9, 11)
				.set(new LineStyle<GPSTrace>().setWidth(0.6f).setColor(Color.BLUE), 12, 20)
				;
		new Tiling(fs, new RasterTileBuilder<GPSTrace>(style), outPath + "default/", zs, false).doTiling();*/




		//make tiles - by date
		//TODO




		//make tiles - by type
		//TODO




		//segment based styles
		System.out.println("Extract GPS segments");

		ArrayList<GPSSegment> segs = new ArrayList<GPSSegment>();
		for(GPSTrace t : traces) segs.addAll(t.getSegments());
		traces = null;
		System.out.println("("+segs.size()+" segments to draw)");
		//new NoGeneralisation<GPSSegment>().perform(segs, zs);


		//make tiles - by segment speed
		System.out.println("Tiling segment speed");

		ColorScale colScale = new ColorScale(){
			Color[] colRamp1 = ColorUtil.getColors(new Color[]{Color.BLUE, Color.GREEN}, 10);
			Color[] colRamp2 = ColorUtil.getColors(new Color[]{Color.GREEN, Color.RED}, 10);
			Color[] colRamp3 = ColorUtil.getColors(new Color[]{Color.RED, Color.YELLOW}, 10);
			public Color getColor(double value) {
				if(value<30)
					return ColorUtil.getColor(colRamp1, value, 5, 30);
				if(value<140)
					return ColorUtil.getColor(colRamp2, value, 30, 140);
				return ColorUtil.getColor(colRamp3, value, 140, 350);
			}
		};
		MultiScaleProperty<Style<GPSSegment>> styleSpeed = new MultiScaleProperty<Style<GPSSegment>>()
				.set(new GPSSegmentSpeedStyle(colScale, 1f), 0, 7)
				.set(new GPSSegmentSpeedStyle(colScale, 1.3f), 8, 20)
				;
		new Tiling(segs, new RasterTileBuilder<GPSSegment>(styleSpeed), outPath + "speed/", zs, false).doTiling();

		/*
		Impossible to parse date: 2016-01-17T14:26:54.610Z
Impossible to parse date: 2016-01-16T11:22:19.750Z
Impossible to parse date: 2016-01-17T09:26:46.830Z
Impossible to parse date: 2016-01-17T15:51:49.650Z
Impossible to parse date: 2016-01-15T12:11:22.560Z
Impossible to parse date: 2016-01-16T13:15:43.610Z
Impossible to parse date: 2016-01-16T15:56:16.650Z
		 */

		System.out.println("Done.");
	}

}
