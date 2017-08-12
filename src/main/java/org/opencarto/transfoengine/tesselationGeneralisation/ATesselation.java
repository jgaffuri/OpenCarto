/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * A tesselation to be generalised. It is a macro agent.
 * 
 * @author julien Gaffuri
 *
 */
public class ATesselation {

	Collection<AUnit> AUnits;

	public Graph graph;
	public Collection<AEdge> AEdges;
	public Collection<ADomain> ADomains;
	//list of stuff holding constraints
	//archipelagos
	//narrow straights/parts
	//straight/corridor
	//narrow part

	public ATesselation(Collection<Feature> units){

		//create unit agents
		AUnits = new HashSet<AUnit>();
		for(Feature unit : units)
			AUnits.add(new AUnit(unit));

		//build topological map
		Collection<MultiPolygon> mps = new HashSet<MultiPolygon>();
		for(Feature unit : units)
			mps.add((MultiPolygon)unit.getGeom());
		graph = GraphBuilder.build(mps);

		//create edge and domain agents
		AEdges = new HashSet<AEdge>();
		for(Edge e : graph.getEdges())
			AEdges.add((AEdge) new AEdge(e).setId(e.getId()));
		ADomains = new HashSet<ADomain>();
		for(Domain d : graph.getDomains())
			ADomains.add((ADomain) new ADomain(d).setId(d.getId()));

		//link domain and units agents
		System.out.println("Link domains and units");
		//build spatial index for units
		SpatialIndex spUnit = new STRtree();
		for(AUnit u : AUnits) spUnit.insert(u.getObject().getGeom().getEnvelopeInternal(), u);
		//for each domain, find unit that intersects and make link
		for(ADomain adom : ADomains){
			Polygon domGeom = adom.getObject().getGeometry();
			List<AUnit> us = spUnit.query(domGeom.getEnvelopeInternal());
			boolean found=false;
			for(AUnit u : us) {
				Geometry uGeom = u.getObject().getGeom();
				if(!uGeom.intersects(domGeom)) continue;
				Geometry inter = uGeom.intersection(domGeom);
				if(inter.getArea()==0) continue;
				found=true;
				//link
				adom.aUnit = u; u.aDomains.add(adom);
				break;
			}
			if(!found) System.err.println("Did not find unit for domain "+adom.getId());
		}

	}




	//TODO desigh activation strategies:
	//agents:
	// 1. meso-border: one border + two units
	// 2. meso-unit: one unit + neighbor units
	//evaluate all constraints - evaluate all agents
	//select (randomly) an unsatisfied agent (unit or border)
	//evaluate meso satisfaction (simply average of components' satisfaction)

}
