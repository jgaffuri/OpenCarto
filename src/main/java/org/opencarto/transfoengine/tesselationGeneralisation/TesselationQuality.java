/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssueType;
import org.opencarto.datamodel.Feature;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.Engine;
import org.opencarto.util.FeatureUtil;

import com.vividsolutions.jts.index.SpatialIndex;

/**
 * @author juju
 *
 */
public class TesselationQuality {
	private final static Logger LOGGER = Logger.getLogger(TesselationQuality.class.getName());



	public static void checkQuality(Collection<Feature> units, double nodingResolution, int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		Partition.runRecursively(units, new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);

				LOGGER.debug("Build spatial indexes");
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

				LOGGER.debug("Run evaluation");
				Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits).sort();
				uEng.runEvaluation("target/eval_units.csv", false).clear();

				t.clear();

			}}, maxCoordinatesNumber, objMaxCoordinateNumber, true);
	}


}
