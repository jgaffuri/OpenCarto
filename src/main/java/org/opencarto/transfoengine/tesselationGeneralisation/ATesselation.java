/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Agent;

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
	public Collection<AFace> aFaces;
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

		//create edge and face agents
		aEdges = new HashSet<AEdge>();
		for(Edge e : graph.getEdges())
			aEdges.add((AEdge) new AEdge(e,this).setId(e.getId()));
		aFaces = new HashSet<AFace>();
		for(Face f : graph.getFaces())
			aFaces.add((AFace) new AFace(f,this).setId(f.getId()));

		System.out.println("Link face and unit agents");
		//build spatial index for units
		SpatialIndex spUnit = new STRtree();
		for(AUnit u : aUnits) spUnit.insert(u.getObject().getGeom().getEnvelopeInternal(), u);
		//for each face, find unit that intersects and make link
		Collection<AFace> enclaveToRemove = new HashSet<AFace>();
		for(AFace aFace : aFaces){
			Polygon faceGeom = aFace.getObject().getGeometry();
			List<AUnit> us = spUnit.query(faceGeom.getEnvelopeInternal());
			boolean found=false;
			for(AUnit u : us) {
				Geometry uGeom = u.getObject().getGeom();
				if(!uGeom.getEnvelopeInternal().intersects(faceGeom.getEnvelopeInternal())) continue;
				//Geometry inter = uGeom.intersection(faceGeom);
				//if(inter.getArea()==0) continue;
				if(!uGeom.covers(faceGeom)) continue;
				found=true;
				//link
				aFace.aUnit = u; u.aFaces.add(aFace);
				break;
			}
			if(!found)
				//System.err.println("Did not find any unit for face "+aFace.getId());
				//case of enclave in dataset: remove the face.
				enclaveToRemove.add(aFace);
		}

		System.out.println("Remove dataset enclaves");
		for(AFace aFace : enclaveToRemove){
			aFaces.remove(aFace);
			graph.removeFace(aFace.getObject());
		}

		System.out.println("   done.");

	}






	//TODO design activation strategies:
	//agents:
	// 1. meso-border: one border + two units
	// 2. meso-unit: one unit + neighbor units
	//evaluate all constraints - evaluate all agents
	//select (randomly) an unsatisfied agent (unit or border)
	//evaluate meso satisfaction (simply average of components' satisfaction)




	public AEdge getAEdge(Edge e){
		for(AEdge ae:aEdges) if(ae.getObject()==e) return ae;
		return null;
	}
	public AFace getAFace(Face f){
		for(AFace af:aFaces) if(af.getObject()==f) return af;
		return null;
	}




	public void exportAgentReport(String outPath) {
		Agent.saveStateReport(aUnits, outPath, "unitsState.txt");
		Agent.saveStateReport(aFaces, outPath, "faceState.txt");
		Agent.saveStateReport(aEdges, outPath, "edgeState.txt");
	}

	public void exportAsSHP(String outPath, int epsg) {
		//GraphSHPUtil.exportAsSHP(t.graph, outPath, 3035);
		exportUnitsAsSHP(outPath, "units.shp", epsg);
		exportFaceAsSHP(outPath, "faces.shp", epsg);
		exportEdgesAsSHP(outPath, "edges.shp", epsg);
		exportNodesAsSHP(outPath, "nodes.shp", epsg);
	}

	public void exportUnitsAsSHP(String outPath, String outFile, int epsg){
		ArrayList<Feature> fs = new ArrayList<Feature>();
		for(AUnit u : aUnits) {
			if(u.isDeleted()) continue;
			u.updateGeomFromFaceGeoms();
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

	public void exportFaceAsSHP(String outPath, String outFile, int epsg) {
		HashSet<Feature> fs = new HashSet<Feature>();
		for(AFace aFace:aFaces) {
			if(aFace.isDeleted()) continue;
			Feature f = aFace.getObject().toFeature();
			if(f.getGeom()==null){
				System.out.println("NB: null geom for face "+aFace.getId());
				continue;
			}
			if(!f.getGeom().isValid()) {
				System.out.println("NB: non valide geometry for face "+aFace.getId());
				continue;
			}
			f.setProjCode(epsg);
			//add unit's id
			f.getProperties().put("unit", aFace.aUnit.getId());
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

	public void exportEdgesAsSHP(String outPath, String outFile, int epsg) {
		HashSet<Feature> fs = new HashSet<Feature>();
		for(AEdge aEdg:aEdges){
			if(aEdg.isDeleted()) continue;
			Feature f = aEdg.getObject().toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

	public void exportNodesAsSHP(String outPath, String outFile, int epsg) {
		SHPUtil.saveSHP(graph.getNodeFeatures(epsg), outPath, outFile);
	}

}
