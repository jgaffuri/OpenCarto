/**
 * 
 */
package org.opencarto;

import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.Engine.Stats;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoSelfIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeToEdgeIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeneGISCO {

	//0.1mm: 1:1M -> 100m
	static double resolution1M = 200;

	public static void main(String[] args) {
		System.out.println("Start");

		//TODO try all scales one by one - from 1M and from 100k
		//TODO gene evaluation - pb detection. run it on 2010 datasets
		//TODO focus on activation strategy
		//TODO create logging mechanism
		//TODO data enrichment step: narrow straights/corridors detection. Archipelagos detection
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO face collapse
		//TODO TEnclaveFaceDeletion should be a special case of face aggregation

		/*
		//TODO upgrade JTS and test new simplification algo
<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.14.0</version>
</dependency>
		 */

		String inputDataPath = "/home/juju/workspace/EuroGeoStat/resources/NUTS/2013/1M/LAEA/lvl3/RG.shp";
		//String inputDataPath = "/home/juju/Bureau/COMM_NUTS_SH/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		////String inputDataPath = "/home/juju/Bureau/COMM_NUTS_SH/COMM_RG_01M_2013_LAEA.shp";
		//String inputDataPath = "/home/juju/Bureau/COMM_NUTS_SH/COMM_RG_100k_2013_LAEA.shp";
		String outPath = "/home/juju/Bureau/out/";

		runNUTSGeneralisation(inputDataPath, 3035, 3*resolution1M, outPath);
		//runNUTSGeneralisationAllScales(inputDataPath, 3035, outPath);

		System.out.println("End");
	}




	static void runNUTSGeneralisation(String inputDataPath, int epsg, double resolution, String outPath) {
		System.out.println("Load data and build tesselation");
		ATesselation t = new ATesselation(SHPUtil.loadSHP(inputDataPath,epsg).fs);

		//use NUTS id as unit id
		for(AUnit uAg : t.aUnits){
			String nutsId = ""+uAg.getObject().getProperties().get("NUTS_ID");
			uAg.setId(nutsId );
			uAg.getObject().id = nutsId;
		}

		System.out.println("Add generalisation constraints");
		double resSqu = resolution*resolution;
		for(AEdge edgAg : t.aEdges){
			edgAg.addConstraint(new CEdgeNoSelfIntersection(edgAg));
			edgAg.addConstraint(new CEdgeToEdgeIntersection(edgAg, t.graph.getSpatialIndexEdge()));
			edgAg.addConstraint(new CEdgeGranularity(edgAg, resolution, true)); //TODO should be something more like shape complexity + add
			edgAg.addConstraint(new CEdgeNoTriangle(edgAg));
			//edgAg.addConstraint(new CEdgeMinimumSize(edgAg, resolution*0.8, resolution));
			//TODO add constraint on edge position?
		}
		for(AFace faceAg : t.aFaces){
			faceAg.addConstraint(new CFaceSize(faceAg, resSqu*0.7, resSqu));
		}



		//engines
		Engine<AFace> fEng = new Engine<AFace>(t.aFaces);
		Engine<AEdge> eEng = new Engine<AEdge>(t.aEdges);

		//TODO include that in engine
		System.out.println("Compute initial satisfaction");
		Stats dStatsIni = fEng.getSatisfactionStats();
		Stats eStatsIni = eEng.getSatisfactionStats();

		System.out.println("Run generalisation");
		fEng.activateQueue();
		eEng.activateQueue();


		//TODO include that in engine
		System.out.println("Compute final satisfaction");
		Stats dStatsFin = fEng.getSatisfactionStats();
		Stats eStatsFin = eEng.getSatisfactionStats();

		//TODO include that in engine
		System.out.println(" --- Initial state ---");
		System.out.println("Edges: "+eStatsIni.median);
		System.out.println("Faces: "+dStatsIni.median);
		System.out.println(" --- Final state ---");
		System.out.println("Edges: "+eStatsFin.median);
		System.out.println("Faces: "+dStatsFin.median);

		System.out.println("Save output");
		t.exportAsSHP(outPath, epsg);
		System.out.println("Save report on agents satisfaction");
		t.exportAgentReport(outPath);
	}


	//generalisation process for all NUTS scales
	static void runNUTSGeneralisationAllScales(String inputDataPath, int epsg, String outPath) {
		//resolutions 0.1mm: 1:1M -> 100m
		for(int scale : new int[]{1,3,10,20,60}){
			System.out.println("--- NUTS generalisation for "+scale+"M");
			runNUTSGeneralisation(inputDataPath, 3035, scale*resolution1M, outPath+scale+"M/");
		}

	}

}
