/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssueType;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.transfoengine.Engine;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * Default procedure for basic generalisation of statistical units tesselations.
 * 
 * @author julien Gaffuri
 *
 */
public class TesselationGeneralisation {
	public final static Logger LOGGER = Logger.getLogger(TesselationGeneralisation.class.getName());


	public static TesselationGeneralisationSpecifications defaultSpecs = new TesselationGeneralisationSpecifications() {
		public void setTesselationConstraints(ATesselation t, CartographicResolution res) {
			//t.addConstraint(new CTesselationMorphology(t, res.getSeparationDistanceMeter(), 1e-5, 5));
		}
		public void setUnitConstraints(ATesselation t, CartographicResolution res) {
			for(AUnit a : t.aUnits) {
				a.addConstraint(new CUnitNoNarrowGaps(a, res.getSeparationDistanceMeter(), 1e-5, 5, true, true).setPriority(10));
			}
		}
		public void setTopologicalConstraints(ATesselation t, CartographicResolution res) {
			for(AFace a : t.aFaces) {
				a.addConstraint(new CFaceSize(a, 0.2*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), true, false).setPriority(2));
				a.addConstraint(new CFaceValidity(a).setPriority(1));
				//a.addConstraint(new CFaceEEZInLand(a).setPriority(10));
				//a.addConstraint(new CFaceNoSmallHoles(a, resSqu*5).setPriority(3));
				//a.addConstraint(new CFaceNoEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()).setPriority(1));
			}
			for(AEdge a : t.aEdges) {
				a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM()));
				a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
				a.addConstraint(new CEdgeValidity(a));
				a.addConstraint(new CEdgeNoTriangle(a));
				//a.addConstraint(new CEdgeSize(a, resolution, resolution*0.6));
				//a.addConstraint(new CEdgeNoSelfIntersection(a));
				//a.addConstraint(new CEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()));
			}
		}
	};


	public static Collection<Feature> runGeneralisation(Collection<Feature> units, HashMap<String, Collection<Point>> points, final TesselationGeneralisationSpecifications specs, double scaleDenominator, final int roundNb, final boolean runGC, int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		final CartographicResolution res = new CartographicResolution(scaleDenominator);
		for(int i=1; i<=roundNb; i++) {
			if(LOGGER.isInfoEnabled()) LOGGER.info("Round "+i);
			final int i_ = i;
			units = Partition.runRecursively(units, new Operation() {
				public void run(Partition p) {
					try {
						//if(LOGGER.isInfoEnabled()) LOGGER.info("R" + i_ + "/" + roundNb + " - " + p.toString());

						//get specifications
						TesselationGeneralisationSpecifications specs_ = specs;
						if(specs_ == null) specs_ = defaultSpecs;

						//build tesselation
						ATesselation t = new ATesselation(p.getFeatures(), p.getEnvelope(), clipPoints(points,p.getEnvelope()));

						LOGGER.debug("   Set tesselation constraints");
						specs_.setTesselationConstraints(t, res);
						LOGGER.debug("   Activate tesselation");
						t.activate();

						Engine<?> eng;

						LOGGER.debug("   Activate units");
						specs_.setUnitConstraints(t, res);
						eng = new Engine<AUnit>(t.aUnits); eng.shuffle().activateQueue().clear();

						//TODO noding - include that as a constraint at tesselation level
						LOGGER.trace("   Ensure noding");
						NodingUtil.fixNoding(NodingIssueType.PointPoint, t.getUnits(), 1e-5);
						NodingUtil.fixNoding(NodingIssueType.LinePoint, t.getUnits(), 1e-5);

						LOGGER.debug("   Create tesselation's topological map");
						t.buildTopologicalMap();
						specs_.setTopologicalConstraints(t, res);
						LOGGER.debug("   Activate faces");
						eng = new Engine<AFace>(t.aFaces); eng.shuffle().activateQueue().clear();
						LOGGER.debug("   Activate edges");
						eng = new Engine<AEdge>(t.aEdges); eng.shuffle().activateQueue().clear();

						//update units' geometries
						for(AUnit u : t.aUnits) {
							if(u.isDeleted()) continue; //TODO keep trace of deleted units to remove them?
							u.updateGeomFromFaceGeoms();
						}
						t.destroyTopologicalMap();
						//TODO remove deleted units here?
						t.clear();

						if(runGC) System.gc();
					} catch (Exception e) { e.printStackTrace(); }
				}}, maxCoordinatesNumber, objMaxCoordinateNumber, false);
			for(Feature unit : units) unit.setGeom(JTSGeomUtil.toMulti(unit.getGeom()));
		}
		return units;
	}

	//
	public static HashMap<String,Collection<Point>> loadPoints(String filePath, String idProp) {
		HashMap<String,Collection<Point>> index = new HashMap<String,Collection<Point>>();
		for(Feature f : SHPUtil.loadSHP(filePath).fs) {
			String id = f.getProperties().get(idProp).toString();
			if(id == null) {
				LOGGER.warn("Could not find id "+idProp+" in file "+filePath);
				return null;
			}
			if("".equals(id)) continue;
			Collection<Point> data = index.get(id);
			if(data == null) { data=new ArrayList<Point>(); index.put(id, data); }
			data.add((Point) f.getGeom());
		}
		return index;
	}

	static HashMap<String, Collection<Point>> clipPoints(HashMap<String, Collection<Point>> points, Envelope env) {
		HashMap<String, Collection<Point>> points_ = new HashMap<String, Collection<Point>>();
		for(Entry<String,Collection<Point>> e : points.entrySet()) {
			Collection<Point> col = new ArrayList<Point>();
			for(Point pt : e.getValue()) if(env.contains(pt.getCoordinate())) col.add(pt);
			if(col.size()==0) continue;
			points_.put(e.getKey(), col);
		}
		return points_ ;
	}


	/*
	public static void runEvaluation(ATesselation t, String outPath, double satisfactionThreshold){

		new File(outPath).mkdirs();
		Engine<AFace> fEng = new Engine<AFace>(t.aFaces, null).sort();
		fEng.runEvaluation(outPath+"eval_faces.csv", true);
		Engine<AEdge> eEng = new Engine<AEdge>(t.aEdges, null).sort();
		eEng.runEvaluation(outPath+"eval_edges.csv", true);
		Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, null).sort();
		uEng.runEvaluation(outPath+"eval_units.csv", true);

		try {
			String reportFilePath = outPath + "eval_report.txt";
			File f = new File(reportFilePath); if(f.exists()) f.delete();
			f.createNewFile();
			PrintWriter lw = new PrintWriter(reportFilePath);

			//print stats on agents' satisfaction
			Stats s = fEng.getSatisfactionStats(false);
			lw.println("--- Faces ---");
			lw.println(s.getSummary());
			s = eEng.getSatisfactionStats(false);
			lw.println("--- Edges ---");
			lw.println(s.getSummary());
			s = uEng.getSatisfactionStats(false);
			lw.println("--- Units ---");
			lw.println(s.getSummary());

			//get and print most problematic constraints
			lw.println("-----------");
			ArrayList<Constraint> cs = new ArrayList<Constraint>();
			cs.addAll( Engine.getUnsatisfiedConstraints(t.aFaces, satisfactionThreshold) );
			cs.addAll( Engine.getUnsatisfiedConstraints(t.aEdges, satisfactionThreshold) );
			cs.addAll( Engine.getUnsatisfiedConstraints(t.aUnits, satisfactionThreshold) );
			lw.println(cs.size()+" constraints have a satisfaction below "+satisfactionThreshold);
			Collections.sort(cs, Constraint.COMPARATOR_CONSTR_BY_SATISFACTION);
			Collections.reverse(cs);
			for(Constraint c : cs) lw.println(c.getMessage());

			lw.close();
		} catch (Exception e) { e.printStackTrace(); }

	}*/

}
