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
import org.opencarto.transfoengine.tesselationGeneralisation.DomainAgent;
import org.opencarto.transfoengine.tesselationGeneralisation.DomainSizeConstraint;
import org.opencarto.transfoengine.tesselationGeneralisation.EdgeAgent;
import org.opencarto.transfoengine.tesselationGeneralisation.EdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.EdgeNoSelfIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.EdgeToEdgeIntersection;
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
		//String inputDataPath = "/home/juju/Bureau/COMM_NUTS_SH/COMM_RG_01M_2013_LAEA.shp";
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
			Agent domAg = new DomainAgent(d).setId(d.getId());
			domAg.addConstraint(new DomainSizeConstraint(domAg, resSqu*0.7, resSqu));
			domAgs.add(domAg);
		}

		//create edge agents and attach constraints
		Collection<Agent> edgAgs = new HashSet<Agent>();
		for(Edge e : graph.getEdges()) {
			Agent edgAg = new EdgeAgent(e).setId(e.getId());
			edgAg.addConstraint(new EdgeNoSelfIntersection(edgAg));
			edgAg.addConstraint(new EdgeToEdgeIntersection(edgAg, graph.getSpatialIndexEdge()));
			edgAg.addConstraint(new EdgeGranularity(edgAg, resolution)); //TODO should be something more like shape complexity
			//TODO add constraint on edge position?
			edgAgs.add(edgAg);
		}


		//track
		//activate edge agents
		//TODO agent scheduler?
		for(Agent agent : edgAgs) {
			agent.activate();
		}



		//save report on domain agent satisfaction
		Agent.saveStateReport(domAgs, outPath, "domainState.txt");

		//save report on domain agent satisfaction
		Agent.saveStateReport(edgAgs, outPath, "edgeState.txt");

		//save output as shp files
		GraphSHPUtil.exportAsSHP(graph, outPath, 3035);

		System.out.println("End");
	}


	//*** constraints/measure/algo
	//A. border granularity / minimum segment size / simplification: DP, wis, cusmoo, etc.
	//B. border topology: no self overlap / topological query / none
	//C. unit topology: no self overlap / topological query / none
	//D. border minimum size / length / segment enlargement (GAEL) - segment colapse (integrate, GAEL)
	//E. unit area (or part) / area / scaling (GAEL), area colapse (integrate, GAEL), skeletisation
	//F. unit shape / convexity, elongation, etc. / none
	//G. border shape & position / Hausdorf distance / none

	//*** generalisation engine
	//agents: borders and units
	//evaluation: based on constraints measures and severity functions
	//activation strategies:
	// 1. meso-border: one border + two units
	// 2. meso-unit: one unit + neighbor units

	//evaluate all constraints - evaluate all agents
	//select (randomly) an unsatisfied agent (unit or border)
	//evaluate meso satisfaction (simply average of components' satisfaction)
	//get best algo to apply, apply it
	//if result is improved, keep it or go back to previous step


	//can be adapted for cartogram generation?

}
