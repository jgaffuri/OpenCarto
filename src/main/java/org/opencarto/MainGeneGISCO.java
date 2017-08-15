/**
 * 
 */
package org.opencarto;

import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.Engine.Stats;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoSelfIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeToEdgeIntersection;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeneGISCO {

	public static void main(String[] args) {
		System.out.println("Start");

		//TODO gene evaluation - pb detection
		//TODO test all scales
		//TODO focus on activation strategy
		//TODO create logging mechanism
		//TODO data enrichment step: narrow straights/corridors detection. Archipelagos detection
		//TODO ... make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO face collapse
		//TODO TEnclaveFaceDeletion should be a special case of face aggregation

		//TODO upgrade JTS and test new simplification algo
		/*

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

		System.out.println("Load data and build tesselation");
		ATesselation t = new ATesselation(SHPUtil.loadSHP(inputDataPath,3035).fs);

		//use NUTS id as unit id
		for(AUnit uAg : t.aUnits){
			String nutsId = ""+uAg.getObject().getProperties().get("NUTS_ID");
			uAg.setId(nutsId );
			uAg.getObject().id = nutsId;
		}

		System.out.println("Add generalisation constraints");
		//resolutions 0.2mm: 1:1M -> 200m
		//1M 3M 10M 20M 60M
		double resolution = 2000, resSqu = resolution*resolution;
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
		t.exportAsSHP(outPath, 3035);
		System.out.println("Save report on agents satisfaction");
		t.exportAgentReport(outPath);

		System.out.println("End");
	}

}
