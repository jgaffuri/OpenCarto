package org.opencarto;

import java.io.File;
import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitNoding;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

public class MainGISCOGeometryNoding {

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath + "out/100k_1M/comm/out_narrow_gaps_removed.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("ADM0_CODE") != null) f.id = ""+f.getProperties().get("ADM0_CODE");
			else if(f.getProperties().get("ADM0_NAME") != null) f.id = ""+f.getProperties().get("ADM_NAME");

		ATesselation t = new ATesselation(fs);

		//build spatial index for units
		SpatialIndex index = new STRtree();
		for(AUnit a : t.aUnits) index.insert(a.getObject().getGeom().getEnvelopeInternal(), a.getObject());


		//LOGGER.info("   Set units constraints");
		for(AUnit a : t.aUnits) {
			//if(!"GL9055".equals(a.getId())) continue;
			if(!"AT10820".equals(a.getId())) continue;
			a.addConstraint(new CUnitNoding(a, index, 1e-5));
		}

		//DefaultTesselationGeneralisation.runEvaluation(t, "/home/juju/Bureau/qual_cont/", 10);
		Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, null).sort();

		//String outPath = "/home/juju/Bureau/qual_cont/";
		//new File(outPath).mkdirs();
		//uEng.runEvaluation(outPath + "eval_units_noding.csv", true);

		/*uEng.activateQueue();
		String outPath = basePath + "out/";
		SHPUtil.saveSHP(t.getUnits(epsg), outPath+ "100k_1M/comm/", "out_narrow_gaps_removed_noded.shp");*/

		System.out.println("End");
	}

}
