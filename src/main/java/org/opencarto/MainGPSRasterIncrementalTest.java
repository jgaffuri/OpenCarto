package org.opencarto;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;

import org.opencarto.datamodel.MultiScaleProperty;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.style.Style;
import org.opencarto.style.basic.LineStyle;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.raster.RasterTileBuilder;
import org.opencarto.util.ColorUtil;

public class MainGPSRasterIncrementalTest {

	public static void main(String[] args) throws ParseException {
		String path1 = "/home/juju/GPS/gpx_test/20161120-074849-Run.gpx";
		String path2 = "/home/juju/GPS/gpx_test/20161124-114456-Walk.gpx";

		String outPath = "/home/juju/GPS/app_raster/gps_traces_raster/";
		ZoomExtend zs = new ZoomExtend(0,14);


		//styles based on traces

		//load traces
		ArrayList<GPSTrace> traces = new ArrayList<GPSTrace>();
			traces.addAll( GPSUtil.loadTraces(new File(path1)) );
		System.out.println(traces.size() + " traces loaded.");

		//make generalisation
		//new NoGeneralisation<GPSTrace>().perform(fs, zs);
		new DefaultGeneralisation<GPSTrace>(false).perform(traces, zs);



		//make tiles - default
		System.out.println("Tiling default");
		MultiScaleProperty<Style<GPSTrace>> style = new MultiScaleProperty<Style<GPSTrace>>()
				.set(new LineStyle<GPSTrace>().setWidth(1f).setColor(ColorUtil.RED), 0, 8)
				.set(new LineStyle<GPSTrace>().setWidth(0.8f).setColor(ColorUtil.RED), 9, 11)
				.set(new LineStyle<GPSTrace>().setWidth(0.6f).setColor(ColorUtil.RED), 12, 20)
				;
		new Tiling<GPSTrace>(traces, new RasterTileBuilder<GPSTrace>(style), outPath + "defaultInc/", zs, false)
		.doTiling(true);

		System.out.println("Done.");
	}

}
