/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;

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

	public Collection<AUnit> aUnits;

	public Graph graph;
	public Collection<AEdge> aEdges;
	public Collection<ADomain> aDomains;
	//list of stuff holding constraints
	//archipelagos
	//narrow straights/parts
	//straight/corridor
	//narrow part

	public ATesselation(Collection<Feature> units){

		//create unit agents
		aUnits = new HashSet<AUnit>();
		for(Feature unit : units)
			aUnits.add(new AUnit(unit));

		//build topological map
		Collection<MultiPolygon> mps = new HashSet<MultiPolygon>();
		for(Feature unit : units)
			mps.add((MultiPolygon)unit.getGeom());
		graph = GraphBuilder.build(mps);

		//create edge and domain agents
		aEdges = new HashSet<AEdge>();
		for(Edge e : graph.getEdges())
			aEdges.add((AEdge) new AEdge(e).setId(e.getId()));
		aDomains = new HashSet<ADomain>();
		for(Domain d : graph.getDomains())
			aDomains.add((ADomain) new ADomain(d).setId(d.getId()));

		//link domain and units agents
		System.out.println("Link domains and units");
		//build spatial index for units
		SpatialIndex spUnit = new STRtree();
		for(AUnit u : aUnits) spUnit.insert(u.getObject().getGeom().getEnvelopeInternal(), u);
		//for each domain, find unit that intersects and make link
		Collection<ADomain> enclaveToRemove = new HashSet<ADomain>();
		for(ADomain adom : aDomains){
			Polygon domGeom = adom.getObject().getGeometry();
			List<AUnit> us = spUnit.query(domGeom.getEnvelopeInternal());
			boolean found=false;
			for(AUnit u : us) {
				Geometry uGeom = u.getObject().getGeom();
				if(!uGeom.getEnvelopeInternal().intersects(domGeom.getEnvelopeInternal())) continue;
				Geometry inter = uGeom.intersection(domGeom);
				if(inter.getArea()==0) continue;
				found=true;
				//link
				adom.aUnit = u; u.aDomains.add(adom);
				break;
			}
			if(!found)
				//System.err.println("Did not find any unit for domain "+adom.getId());
				//case of enclave in dataset: remove the domain.
				enclaveToRemove.add(adom);
		}

		System.out.println("Remove dataset enclaves");
		for(ADomain adom : enclaveToRemove){
			aDomains.remove(adom);
			graph.removeDomain(adom.getObject());
		}

		System.out.println("   done.");

	}




	//TODO desigh activation strategies:
	//agents:
	// 1. meso-border: one border + two units
	// 2. meso-unit: one unit + neighbor units
	//evaluate all constraints - evaluate all agents
	//select (randomly) an unsatisfied agent (unit or border)
	//evaluate meso satisfaction (simply average of components' satisfaction)



	public void exportUnitsAsSHP(String outPath, String outFile, int epsg){
		ArrayList<Feature> fs = new ArrayList<Feature>();
		for(AUnit u : aUnits) {
			if(u.isDeleted()) continue;
			u.updateGeomFromDomainGeoms();
			Feature f = u.getObject();
			if(f.getGeom()==null){
				System.out.println("NB: null geom for unit "+u.getId());
				continue;
			}
			if(!f.getGeom().isValid()) {
				System.out.println("NB: non valide geometry for unit "+u.getId());
			}
			f.setProjCode(epsg);

			//u.computeSatisfaction();
			//f.getProperties().put("satis", u.getSatisfaction());

			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

}
