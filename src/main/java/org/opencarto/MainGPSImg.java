package org.opencarto;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.style.MultiScaleStyle;
import org.opencarto.style.basic.LineStyle;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.raster.RasterTileBuilder;

public class MainGPSImg {
	static String outPath_;

	public static void main(String[] args) {
		String[] inPaths = new String[] {"/home/juju/GPS/strava/","/home/juju/GPS/gpx/"};
		//String[] inPaths = new String[] {"/home/juju/GPS/gpx_test/"};
		String outPath = "/home/juju/Bureau/GPS_img_tiles/";
		int zoomMax = 14;

		if(args.length == 3){
			inPaths = new String[] {args[0]};
			outPath = args[1];
			zoomMax = Integer.parseInt(args[2]);
		}

		outPath_ = outPath;
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

		//make tiles
		System.out.println("Tiling");
		MultiScaleStyle style = new MultiScaleStyle()
				.setStyle(new LineStyle().setWidth(1.3f).setColor(Color.BLUE), 0, 8)
				.setStyle(new LineStyle().setWidth(1f).setColor(Color.BLUE), 9, 11)
				.setStyle(new LineStyle().setWidth(0.7f).setColor(Color.BLUE), 12, 20)
				;
		new Tiling(fs, new RasterTileBuilder(style), outPath, zs, false).doTiling();

		System.out.println("Done.");
	}

}
