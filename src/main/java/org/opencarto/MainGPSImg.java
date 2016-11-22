package org.opencarto;

import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.raster.RasterTileBuilder;

public class MainGPSImg {
	static String outPath_;

	public static void main(String[] args) {
		String inPath = "/home/juju/GPS/gpx_test2/";
		String outPath = "/home/juju/workspace/opencarto-code/client/war/data/GPS_tiles/";
		int zoomMax = 10;

		if(args.length == 3){
			inPath = args[0];
			outPath = args[1];
			zoomMax = Integer.parseInt(args[2]);
		}

		outPath_ = outPath;
		ZoomExtend zs = new ZoomExtend(0,zoomMax);

		//load traces
		System.out.println("Load traces in "+inPath);
		File[] files = new File(inPath).listFiles();
		ArrayList<GPSTrace> fs = GPSUtil.load(files);
		System.out.println(fs.size() + " traces loaded.");

		//make generalisation
		//new NoGeneralisation<GPSTrace>().perform(fs, zs);
		new DefaultGeneralisation<GPSTrace>(false).perform(fs, zs);

		//make tiles
		System.out.println("Tiling");
		new Tiling(fs, new RasterTileBuilder(), outPath, zs, false).doTiling();

		System.out.println("Done.");
	}

}
