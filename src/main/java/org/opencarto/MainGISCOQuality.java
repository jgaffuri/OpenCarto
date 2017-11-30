package org.opencarto;

import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitOverlap;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitValidity;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOQuality {

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_1M.shp",epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_100k.shp",epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"comm_2013/COMM_RG_100k_2013_LAEA.shp",epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"commplus_100k/COMMPLUS_0404_WM.shp", epsg).fs;
		final int epsg = 4258; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"gaul/GAUL_CLEAN.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"gaul/GAUL_CLEAN_WM.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("ADM0_CODE") != null) f.id = ""+f.getProperties().get("ADM0_CODE");
			else if(f.getProperties().get("ADM0_NAME") != null) f.id = ""+f.getProperties().get("ADM_NAME");

		ATesselation t = new ATesselation(fs);

		//build spatial index for units
		SpatialIndex index = new Quadtree();
		for(AUnit a : t.aUnits) index.insert(a.getObject().getGeom().getEnvelopeInternal(), a.getObject());

		//LOGGER.info("   Set units constraints");
		for(AUnit a : t.aUnits) {
			a.addConstraint(new CUnitOverlap(a, index));
			a.addConstraint(new CUnitValidity(a));
		}

		//DefaultTesselationGeneralisation.runEvaluation(t, "/home/juju/Bureau/qual_cont/", 10);
		Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, null).sort();
		String outPath = "/home/juju/Bureau/qual_cont/";
		new File(outPath).mkdirs();
		uEng.runEvaluation(outPath+"eval_units.csv", true);

		System.out.println("End");
	}

}
