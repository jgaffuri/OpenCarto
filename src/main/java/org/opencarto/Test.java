package org.opencarto;

import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.processes.NoGeneralisation;
import org.opencarto.processes.SHPProcesses;
import org.opencarto.tiling.description.DefaultDescriptionBuilder;

public class Test {

	public static void main(String[] args) throws Exception {
		System.out.println("Start");

		//String shpPath = "data/GEOFLA/COMMUNE.shp";
		String shpPath = "data/NUTS_2013_01M_SH/NUTS_BN_01M_2013.shp";
		String outPath = "tiles/";

		/*
		System.out.println("Load data");
		Collection<? extends Feature> fs = SHPUtil.loadShp(shpPath, new String[]{"INSEE_COM"});
		System.out.println(fs.size()+" features loaded.");

		//TODO generalisation

		System.out.println("Tiling");
		new Tiling(fs, new VectorTileBuilder(), outPath, new ZoomExtend(0,5), true).doTiling();
		 */

		SHPProcesses.perform(shpPath, new String[]{"EU_FLAG"}, outPath, new ZoomExtend(0,4), new NoGeneralisation(), new DefaultDescriptionBuilder());

		System.out.println("Done.");
	}

}
