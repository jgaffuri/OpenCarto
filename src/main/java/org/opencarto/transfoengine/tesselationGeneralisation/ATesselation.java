/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;

/**
 * A tesselation to be generalised. It is a macro agent.
 * 
 * @author julien Gaffuri
 *
 */
public class ATesselation {

	Collection<AUnit> AUnits;

	Graph graph;
	Collection<AEdge> AEdges;
	Collection<ADomain> ADomains;
	//archipelagos
	//narrow straights/parts
	//straight/corridor
	//narrow part

	public ATesselation(Collection<Feature> units){
		//TODO
	}


	//list of stuff holding constraints


	//TODO desigh activation strategies:
	//agents:
	// 1. meso-border: one border + two units
	// 2. meso-unit: one unit + neighbor units
	//evaluate all constraints - evaluate all agents
	//select (randomly) an unsatisfied agent (unit or border)
	//evaluate meso satisfaction (simply average of components' satisfaction)

}
