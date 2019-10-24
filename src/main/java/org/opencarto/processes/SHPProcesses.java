package org.opencarto.processes;

import java.util.ArrayList;

import org.opencarto.MultiScaleFeature;
import org.opencarto.ZoomExtend;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.description.Description;
import org.opencarto.tiling.description.DescriptionBuilder;
import org.opencarto.tiling.vector.VectorTileBuilder;
import org.opengis.filter.Filter;

import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
import eu.europa.ec.eurostat.jgiscotools.io.SHPUtil;
import eu.europa.ec.eurostat.jgiscotools.util.ProjectionUtil;

public class SHPProcesses {

	public static void perform(String shpFilePath, String outPath, int epsgCode, ZoomExtend zs, GeneralisationProcess<MultiScaleFeature> gp, DescriptionBuilder db, boolean withReport, Filter f){
		//load data
		System.out.println("Load "+shpFilePath);
		ArrayList<Feature> fs = SHPUtil.loadSHP(shpFilePath, f).fs;
		System.out.println(fs.size() + " objects loaded.");

		System.out.println("Project to WM");
		ProjectionUtil.toWebMercator(fs, SHPUtil.getCRS(shpFilePath));

		//generalise
		System.err.println("NOT IMPLEMENTED in SHPProcesses");
		//TODO gp.perform(fs, zs);

		if(db != null){
			//export descriptions
			System.out.println("Descriptions");
			Description.export(fs, outPath, db, true);
		}

		//make tiles
		System.out.println("Tiling");
		new Tiling(fs, new VectorTileBuilder(), outPath, zs, withReport).doTiling();
	}

}
