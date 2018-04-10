/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssueType;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.Engine;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;
import org.opencarto.util.ProjectionUtil.CRSType;

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
	public static boolean tracePartitioning = true;

	public static Collection<Feature> runGeneralisation(Collection<Feature> units, HashMap<String, Collection<Point>> points, CRSType crsType, double scaleDenominator, final int roundNb, int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		TesselationGeneralisationSpecification specs = new TesselationGeneralisationSpecification(scaleDenominator, crsType);
		return runGeneralisation(units, points, specs, roundNb, maxCoordinatesNumber, objMaxCoordinateNumber);
	}

	public static Collection<Feature> runGeneralisation(Collection<Feature> units, HashMap<String, Collection<Point>> points, final TesselationGeneralisationSpecification specs, int roundNb, int maxCoordinatesNumber, int objMaxCoordinateNumber) {
		for(int i=1; i<=roundNb; i++) {
			if(LOGGER.isInfoEnabled()) LOGGER.info("Round "+i+" - CoordNb="+FeatureUtil.getVerticesNumber(units)+" FeatNb="+units.size());
			final int i_ = i;
			units = Partition.runRecursively(units, new Operation() {
				public void run(Partition p) {
					try {
						if(LOGGER.isInfoEnabled() && tracePartitioning) LOGGER.info("R" + i_ + "/" + roundNb + " - " + p.toString());

						//build tesselation
						ATesselation t = new ATesselation(p.getFeatures(), p.getEnvelope(), clipPoints(points,p.getEnvelope()));

						Engine<?> eng;

						LOGGER.debug("   Activate units");
						specs.setUnitConstraints(t);
						//TODO activate smaller first?
						eng = new Engine<AUnit>(t.aUnits); eng.shuffle().activateQueue().clear();

						LOGGER.trace("   Ensure noding");
						NodingUtil.fixNoding(NodingIssueType.PointPoint, t.getUnits(), specs.getNodingResolution());
						NodingUtil.fixNoding(NodingIssueType.LinePoint, t.getUnits(), specs.getNodingResolution());

						LOGGER.debug("   Create tesselation's topological map");
						t.buildTopologicalMap();
						specs.setTopologicalConstraints(t);
						LOGGER.debug("   Activate faces");
						//TODO activate smaller first?
						eng = new Engine<AFace>(t.aFaces); eng.shuffle().activateQueue().clear();
						LOGGER.debug("   Activate edges");
						//TODO activate longest first?
						eng = new Engine<AEdge>(t.aEdges); eng.shuffle().activateQueue().clear();

						//update units' geometries
						for(AUnit u : t.aUnits) {
							if(u.isDeleted()) continue; //TODO keep trace of deleted units to remove them?
							u.updateGeomFromFaceGeoms();
						}
						t.destroyTopologicalMap();
						//TODO remove deleted units here?
						t.clear();

						//if(runGC) System.gc();
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
		if(points == null) return null;
		HashMap<String, Collection<Point>> points_ = new HashMap<String, Collection<Point>>();
		for(Entry<String,Collection<Point>> e : points.entrySet()) {
			Collection<Point> col = new ArrayList<Point>();
			for(Point pt : e.getValue()) if(env.contains(pt.getCoordinate())) col.add(pt);
			if(col.size()==0) continue;
			points_.put(e.getKey(), col);
		}
		return points_ ;
	}


	public static void main(String[] args) {
		LOGGER.info("Start");

		//http://ant.apache.org/manual/tutorial-HelloWorldWithAnt.html
		//http://osgeo-org.1560.x6.nabble.com/java-lang-RuntimeException-Unable-to-find-function-Length-td4322100.html

		Options options = new Options();
		options.addOption(Option.builder("i").longOpt("inputFile").desc("Input file (SHP format)")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("o").longOpt("outputFile").desc("Output file (SHP format)")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("ip").longOpt("inputPointFile").desc("Input file for points (SHP format)")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("id").desc("Id property to link the units and the points")
				.hasArg().argName("string").build());
		options.addOption(Option.builder("crs").desc("The EPSG code of the CRS")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("crst")
				.hasArg().argName("string").build());
		options.addOption(Option.builder("s").longOpt("scaleDenominator").desc("The scale denominator for the target data")
				.hasArg().argName("double").build());
		options.addOption(Option.builder("inb").longOpt("roundNb").desc(  "Number of iterations of the process" )
				.hasArg().argName("int").build());
		options.addOption(Option.builder("mcn").longOpt("maxCoordinatesNumber")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("omcn").longOpt("objMaxCoordinateNumber")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("h").desc("Show this help message").build());

		CommandLine cmd = null;
		try { cmd = new DefaultParser().parse( options, args); } catch (ParseException e) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			return;
		}

		//help statement
		if(cmd.hasOption("h")) {
			new HelpFormatter().printHelp("ant", options );
			return;
		}

		LOGGER.info("Set parameters");
		//String inFile = "src/test/resources/testTesselationGeneralisation.shp";
		String inFile = cmd.getOptionValue("i");
		String outFile = cmd.getOptionValue("o");
		if(outFile == null) outFile = new File(inFile).getParent().toString() + "/out.shp";
		String inPtFile = cmd.getOptionValue("ip");
		String idProp = cmd.getOptionValue("id");
		int epsg = cmd.getOptionValue("crs") != null? Integer.parseInt(cmd.getOptionValue("crs")) : -1;
		//CRSType.CARTO
		CRSType crsType = CRSType.CARTO; //TODO
		double scaleDenominator = cmd.getOptionValue("s") != null? Integer.parseInt(cmd.getOptionValue("s")) : 50000;
		int roundNb = cmd.getOptionValue("inb") != null? Integer.parseInt(cmd.getOptionValue("inb")) : 10;
		int maxCoordinatesNumber = cmd.getOptionValue("mcn") != null? Integer.parseInt(cmd.getOptionValue("mcn")) : 1000000;
		int objMaxCoordinateNumber = cmd.getOptionValue("omcn") != null? Integer.parseInt(cmd.getOptionValue("omcn")) : 1000;



		LOGGER.info("Load data from "+inFile);
		Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
		if(idProp != null && !"".equals(idProp)) for(Feature unit : units) unit.id = unit.getProperties().get(idProp).toString();

		HashMap<String, Collection<Point>> points = null;
		if(inPtFile != null && !"".equals(inPtFile)) {
			LOGGER.info("Load point data from "+inPtFile);
			points = TesselationGeneralisation.loadPoints(inPtFile, idProp);
		}

		LOGGER.info("Launch generalisation");
		units = TesselationGeneralisation.runGeneralisation(units, points, crsType, scaleDenominator, roundNb, maxCoordinatesNumber, objMaxCoordinateNumber);

		LOGGER.info("Save output to "+outFile);
		SHPUtil.saveSHP(units, outFile);

		LOGGER.info("End");
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
