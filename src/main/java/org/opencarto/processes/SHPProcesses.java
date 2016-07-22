package org.opencarto.processes;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.io.SHPUtil;
import org.opencarto.io.SHPUtil.SelectionFilter;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.description.Description;
import org.opencarto.tiling.description.DescriptionBuilder;
import org.opencarto.tiling.vector.VectorTileBuilder;
import org.opencarto.util.ProjectionUtil;

public class SHPProcesses {

	public static void perform(String inFile, String outPath, ZoomExtend zs, GeneralisationProcess gp, DescriptionBuilder db, boolean withReport){
		//load data
		System.out.println("Load "+inFile);
		ArrayList<Feature> fs = SHPUtil.loadShp(inFile);
		System.out.println(fs.size() + " objects loaded.");

		System.out.println("Project to WM");
		ProjectionUtil.toWebMercator(fs, SHPUtil.getCRS(inFile));

		//generalise
		gp.perform(fs, zs);

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
