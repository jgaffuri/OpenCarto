package org.opencarto;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssueType;
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
import org.opencarto.util.FeatureUtil;

import com.vividsolutions.jts.index.SpatialIndex;

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

		final double nodingResolution = 1e-7;

		LOGGER.info("Load data");
		ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_1M.shp").fs;
		for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

		LOGGER.info("Run quality check");
		Partition.runRecursively(fs, new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);

				//LOGGER.info("Build spatial indexes");
				SpatialIndex index = FeatureUtil.getSTRtree(p.features);
				SpatialIndex indexLP = FeatureUtil.getSTRtreeCoordinates(p.features);
				SpatialIndex indexPP = NodingUtil.getSTRtreeCoordinatesForPP(p.features, nodingResolution);

				ATesselation t = new ATesselation(p.getFeatures());
				//LOGGER.info("Set constraints");
				for(AUnit a : t.aUnits) {
					a.clearConstraints();
					a.addConstraint(new CUnitOverlap(a, index));
					a.addConstraint(new CUnitNoding(a, indexLP, NodingIssueType.LinePoint, nodingResolution));
					a.addConstraint(new CUnitNoding(a, indexPP, NodingIssueType.PointPoint, nodingResolution));
					a.addConstraint(new CUnitValidity(a));
				}

				//LOGGER.info("Run evaluation");
				Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, null).sort();
				uEng.runEvaluation(outPath+"eval_units.csv", false);
				uEng.clear();

				t.clear();

			}}, 3000000, 15000, true);

		System.out.println("End");
	}

}
