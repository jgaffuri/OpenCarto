/**
 * 
 */
package org.opencarto;

import org.opencarto.io.GraphSHPUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.Engine.Stats;
import org.opencarto.transfoengine.tesselationGeneralisation.ADomain;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.CDomainSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoSelfIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeToEdgeIntersection;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeneGISCO {

	//resolutions 0.2mm: 1:1M -> 200m


	public static void main(String[] args) {
		System.out.println("Start");

		String inputDataPath = "/home/juju/workspace/EuroGeoStat/resources/NUTS/2013/1M/LAEA/lvl3/RG.shp";
		//String inputDataPath = "/home/juju/Bureau/COMM_NUTS_SH/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		//String inputDataPath = "/home/juju/Bureau/COMM_NUTS_SH/COMM_RG_01M_2013_LAEA.shp";
		//String inputDataPath = "/home/juju/Bureau/COMM_NUTS_SH/COMM_RG_100k_2013_LAEA.shp";
		String outPath = "/home/juju/Bureau/out/";

		//load data and build tesselation
		ATesselation t = new ATesselation(SHPUtil.loadSHP(inputDataPath).fs);


		double resolution = 2000, resSqu = resolution*resolution;


		//TODO create 'top' units
		//TODO see transformation.cancel();
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO delete too short edges with only two vertices. edge collapse. length below threshold
		//TODO create logging mechanism
		//TODO enclave deletion
		//TODO small part aggregation and then amalgamation
		//TODO amalgamation
		//TODO data enrichment step: narrow straights/corridors detection. Archipelagos detection.

		//TODO upgrade JTS and test new simplification algo
		/*

<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.14.0</version>
</dependency>
		 */


		//add constraints
		for(AEdge edgAg : t.AEdges){
			edgAg.addConstraint(new CEdgeNoSelfIntersection(edgAg));
			edgAg.addConstraint(new CEdgeToEdgeIntersection(edgAg, t.graph.getSpatialIndexEdge()));
			edgAg.addConstraint(new CEdgeGranularity(edgAg, resolution)); //TODO should be something more like shape complexity + add
			edgAg.addConstraint(new CEdgeNoTriangle(edgAg));
			//TODO: try other line simplification algorithms: VWSimplifier vws;
			//TODO add constraint on edge position?
		}
		for(ADomain domAg : t.ADomains){
			domAg.addConstraint(new CDomainSize(domAg, resSqu*0.7, resSqu));
		}



		//engines
		Engine<AEdge> eEng = new Engine<AEdge>(t.AEdges);
		Engine<ADomain> dEng = new Engine<ADomain>(t.ADomains);

		//store initial satisfaction
		Stats eStatsIni = eEng.getSatisfactionStats();
		Stats dStatsIni = dEng.getSatisfactionStats();

		//activate agents
		dEng.activateQueue();
		eEng.activateQueue();


		//store final satisfaction
		Stats eStatsFin = eEng.getSatisfactionStats();
		Stats dStatsFin = dEng.getSatisfactionStats();


		System.out.println(" --- Initial state ---");
		System.out.println("Edges: "+eStatsIni.median);
		System.out.println("Domains: "+dStatsIni.median);
		System.out.println(" --- Final state ---");
		System.out.println("Edges: "+eStatsFin.median);
		System.out.println("Domains: "+dStatsFin.median);

		//save report on agents satisfaction
		Agent.saveStateReport(t.ADomains, outPath, "domainState.txt");
		Agent.saveStateReport(t.AEdges, outPath, "edgeState.txt");

		//save output as shp files
		//TODO remove from graph elements based on agent's isDeleted()
		GraphSHPUtil.exportAsSHP(t.graph, outPath, 3035);

		System.out.println("End");
	}

}
