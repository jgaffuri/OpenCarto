package org.opencarto;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitNoding;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitOverlap;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOQualityCheck {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOQualityCheck.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = "/home/juju/Bureau/qual_cont/";
		new File(outPath).mkdirs();

		final double nodingResolution = 1e-2;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_1M.shp",epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_100k.shp",epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"comm_2013/COMM_RG_100k_2013_LAEA.shp",epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"commplus_100k/COMMPLUS_0404_WM.shp", epsg).fs;
		//final int epsg = 4258; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"gaul/GAUL_CLEAN_DICE_DISSOLVE.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"gaul/GAUL_CLEAN_WM.shp", epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath + "out/100k_1M/comm/out_narrow_gaps_removed.shp", epsg).fs;

		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"comm_2013/COMM_RG_100k_2013_LAEA.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"gaul/GAUL_CLEAN_DICE_DISSOLVE_WM.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"eez/EEZ_RG_100K_2013_WM.shp", epsg).fs;
		final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_FINAL_WM.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/noded.shp", epsg).fs;

		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("ADM0_CODE") != null) f.id = ""+f.getProperties().get("ADM0_CODE");
			else if(f.getProperties().get("ADM0_NAME") != null) f.id = ""+f.getProperties().get("ADM_NAME");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");
			else if(f.getProperties().get("GISCO_ID") != null) f.id = ""+f.getProperties().get("GISCO_ID");

		CUnitOverlap.LOGGER.setLevel(Level.OFF);
		CUnitNoding.LOGGER.setLevel(Level.OFF);

		Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);

				ATesselation t = new ATesselation(p.getFeatures());

				//build spatial index for units
				SpatialIndex index = new STRtree();
				for(AUnit a : t.aUnits) index.insert(a.getObject().getGeom().getEnvelopeInternal(), a.getObject());

				//LOGGER.info("   Set units constraints");
				for(AUnit a : t.aUnits) {
					a.clearConstraints();
					a.addConstraint(new CUnitOverlap(a, index));
					a.addConstraint(new CUnitNoding(a, index, nodingResolution));
					//a.addConstraint(new CUnitValidity(a));
				}

				Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, null).sort();
				uEng.runEvaluation(outPath+"eval_units.csv", false);

			}}, fs, 3000000, 15000, true);

		System.out.println("End");
	}

}
