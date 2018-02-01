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
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitValidity;

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

		CUnitOverlap.LOGGER.setLevel(Level.OFF);
		CUnitNoding.LOGGER.setLevel(Level.OFF);

		LOGGER.info("Load data");
		final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_100k.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/out_narrow_gaps_removed___.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/noded.shp", epsg).fs;

		for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);
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
					//a.addConstraint(new CUnitNoding(a, index, nodingResolution));
					//a.addConstraint(new CUnitValidity(a));
				}

				Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, null).sort();
				uEng.runEvaluation(outPath+"eval_units.csv", false);

			}}, fs, 3000000, 15000, true);

		System.out.println("End");
	}

}
