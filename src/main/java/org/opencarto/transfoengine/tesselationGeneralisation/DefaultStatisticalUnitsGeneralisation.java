/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.Engine.Stats;

/**
 * 
 * Default procedure for basic generalisation of statistical units tesselations.
 * 
 * @author julien Gaffuri
 *
 */
public class DefaultStatisticalUnitsGeneralisation {
	private final static Logger LOGGER = Logger.getLogger(DefaultStatisticalUnitsGeneralisation.class);





	public static void setUnitConstraints(ATesselation t, double resolution){
		//double resSqu = resolution*resolution;
		for(AUnit a : t.aUnits) {
			a.addConstraint(new CUnitNoNarrowPartsAndGapsXXX(a).setPriority(10));
		}
	}


	public static void setTopologicalConstraints(ATesselation t, double resolution){
		double resSqu = resolution*resolution;
		for(AFace a : t.aFaces) {
			a.addConstraint(new CFaceSize(a, resSqu*0.7, resSqu, resSqu).setPriority(2));
			a.addConstraint(new CFaceValidity(a).setPriority(1));
			//a.addConstraint(new CFaceNoSmallHoles(a, resSqu*5).setPriority(3));
			//a.addConstraint(new CFaceNoEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()).setPriority(1));
		}
		for(AEdge a : t.aEdges) {
			a.addConstraint(new CEdgeGranularity(a, resolution, true));
			a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
			a.addConstraint(new CEdgeValidity(a));
			a.addConstraint(new CEdgeNoTriangle(a));
			//a.addConstraint(new CEdgeSize(a, resolution, resolution*0.6));
			//a.addConstraint(new CEdgeNoSelfIntersection(a));
			//a.addConstraint(new CEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()));
		}
	}




	//TODO better design activation strategies ?
	public static void run(ATesselation t, double resolution, String logFileFolder){

		LOGGER.info("   Set units constraints");
		setUnitConstraints(t, resolution);

		LOGGER.info("   Activate units");
		Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, logFileFolder+"/units.log");
		uEng.getLogWriter().println("******** Activate units ********");
		uEng.shuffle();  uEng.activateQueue();
		uEng.closeLogger();
		uEng = null;

		LOGGER.info("   Create tesselation's topological map");
		t.buildTopologicalMap();

		LOGGER.info("   Set topological constraints");
		setTopologicalConstraints(t, resolution);

		//engines
		Engine<AFace> fEng = new Engine<AFace>(t.aFaces, logFileFolder+"/faces.log");
		Engine<AEdge> eEng = new Engine<AEdge>(t.aEdges, logFileFolder+"/edges.log");

		//System.out.println("Compute initial satisfaction");
		//Stats dStatsIni = fEng.getSatisfactionStats();
		//Stats eStatsIni = eEng.getSatisfactionStats();

		LOGGER.info("   Activate faces 1");
		fEng.getLogWriter().println("******** Activate faces 1 ********");
		fEng.shuffle();  fEng.activateQueue();
		LOGGER.info("   Activate edges 1");
		eEng.getLogWriter().println("******** Activate edges 1 ********");
		eEng.shuffle(); eEng.activateQueue();
		LOGGER.info("   Activate faces 2");
		fEng.getLogWriter().println("******** Activate faces 2 ********");
		fEng.shuffle();  fEng.activateQueue();
		LOGGER.info("   Activate edges 2");
		eEng.getLogWriter().println("******** Activate edges 2 ********");
		eEng.shuffle(); eEng.activateQueue();

		fEng.closeLogger();
		eEng.closeLogger();

		/*System.out.println("Compute final satisfaction");
		Stats dStatsFin = fEng.getSatisfactionStats();
		Stats eStatsFin = eEng.getSatisfactionStats();

		System.out.println(" --- Initial state ---");
		System.out.println("Edges: "+eStatsIni.median);
		System.out.println("Faces: "+dStatsIni.median);
		System.out.println(" --- Final state ---");
		System.out.println("Edges: "+eStatsFin.median);
		System.out.println("Faces: "+dStatsFin.median);*/
	}

	public void runEvaluation(ATesselation t, String outPath, double satisfactionThreshold){
		new File(outPath).mkdirs();
		Engine<AFace> fEng = new Engine<AFace>(t.aFaces, null);
		fEng.runEvaluation(outPath+"eval_faces.csv", true);
		Engine<AEdge> eEng = new Engine<AEdge>(t.aEdges, null);
		eEng.runEvaluation(outPath+"eval_edges.csv", true);
		Engine<AUnit> uEng = new Engine<AUnit>(t.aUnits, null);
		uEng.runEvaluation(outPath+"eval_units.csv", true);

		try {
			String reportFilePath = outPath + "eval_report.txt";
			File f = new File(reportFilePath); if(f.exists()) f.delete();
			f.createNewFile();
			PrintWriter lw = new PrintWriter(reportFilePath);

			//print stats on agents' satisfaction
			Stats s = fEng.getSatisfactionStats();
			lw.println("--- Faces ---");
			lw.println(s.getSummary());
			s = eEng.getSatisfactionStats();
			lw.println("--- Edges ---");
			lw.println(s.getSummary());
			s = uEng.getSatisfactionStats();
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

	}

}
