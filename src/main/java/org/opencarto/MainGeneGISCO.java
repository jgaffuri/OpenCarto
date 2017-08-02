/**
 * 
 */
package org.opencarto;

import java.util.Collection;
import java.util.HashSet;

import org.geotools.feature.FeatureIterator;
import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.GraphSHPUtil;
import org.opencarto.io.ShapeFile;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.Engine.Stats;
import org.opencarto.transfoengine.tesselationGeneralisation.ADomain;
import org.opencarto.transfoengine.tesselationGeneralisation.CDomainSize;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoSelfIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeToEdgeIntersection;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.MultiPolygon;

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

		//load statistical units
		Collection<MultiPolygon> units = new HashSet<MultiPolygon>();
		ShapeFile inputSHP = new ShapeFile(inputDataPath);
		FeatureIterator<SimpleFeature> it = inputSHP.getFeatures();
		while(it.hasNext())
			units.add((MultiPolygon) it.next().getDefaultGeometry());

		//structure dataset into topological map
		Graph graph = GraphBuilder.build(units);


		double resolution = 2000, resSqu = resolution*resolution;

		//create domain agents and attach constraints
		Collection<Agent> domAgs = new HashSet<Agent>();
		for(Domain d : graph.getDomains()) {
			Agent domAg = new ADomain(d).setId(d.getId());
			domAg.addConstraint(new CDomainSize(domAg, graph, resSqu*0.7, resSqu));
			domAgs.add(domAg);
		}

		//create edge agents and attach constraints
		Collection<Agent> edgAgs = new HashSet<Agent>();
		for(Edge e : graph.getEdges()) {
			Agent edgAg = new AEdge(e).setId(e.getId());
			edgAg.addConstraint(new CEdgeNoSelfIntersection(edgAg));
			edgAg.addConstraint(new CEdgeToEdgeIntersection(edgAg, graph.getSpatialIndexEdge()));
			edgAg.addConstraint(new CEdgeGranularity(edgAg, resolution)); //TODO should be something more like shape complexity + add 
			//TODO: try other line simplification algorithms: VWSimplifier vws;
			//TODO add constraint on edge position?
			edgAgs.add(edgAg);
		}


		//TODO check results - implement changes to improve it, currently:
		//TODO: handle island deletion + enclave/small part amalgamation)
		//TODO: create 'top' units
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO delete too short edges with only two vertices. edge collapse. length below threshold
		//TODO data enrichment step: narrow straights/corridors detection. Archipelagos detection.

		//TODO upgrade JTS and test new simplification algo
/*

<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.14.0</version>
</dependency>

*/

		//TODO activation strategies:
		// 1. meso-border: one border + two units
		// 2. meso-unit: one unit + neighbor units
		//evaluate all constraints - evaluate all agents
		//select (randomly) an unsatisfied agent (unit or border)
		//evaluate meso satisfaction (simply average of components' satisfaction)


		//engines
		Engine eEng = new Engine(edgAgs);
		Engine dEng = new Engine(domAgs);

		//store initial satisfaction
		Stats eStatsIni = eEng.getSatisfactionStats();
		Stats dStatsIni = dEng.getSatisfactionStats();

		//activate edge agents
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

		//save report on domain agent satisfaction
		Agent.saveStateReport(domAgs, outPath, "domainState.txt");

		//save report on domain agent satisfaction
		Agent.saveStateReport(edgAgs, outPath, "edgeState.txt");

		//save output as shp files
		//TODO remove from graph elements based on agent's isDeleted()
		GraphSHPUtil.exportAsSHP(graph, outPath, 3035);

		System.out.println("End");
	}

}
