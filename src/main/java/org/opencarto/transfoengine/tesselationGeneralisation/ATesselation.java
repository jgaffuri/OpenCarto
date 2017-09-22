/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
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
public class ATesselation extends Agent {
	private final static Logger LOGGER = Logger.getLogger(ATesselation.class);

	public Collection<AUnit> aUnits;

	public Graph graph;
	public Collection<AEdge> aEdges;
	public Collection<AFace> aFaces;


	public ATesselation(Collection<Feature> units){
		super(null);

		//create unit agents
		aUnits = new HashSet<AUnit>();
		for(Feature unit : units)
			aUnits.add(new AUnit(unit));

	}


	//build topological map
	public ATesselation buildTopologicalMap() {

		//get unit's geometries
		Collection<MultiPolygon> mps = new HashSet<MultiPolygon>();
		for(AUnit au : aUnits)
			mps.add((MultiPolygon)au.getObject().getGeom());
		graph = GraphBuilder.build(mps);

		//create edge and face agents
		aEdges = new HashSet<AEdge>();
		for(Edge e : graph.getEdges())
			aEdges.add((AEdge) new AEdge(e,this).setId(e.getId()));
		aFaces = new HashSet<AFace>();
		for(Face f : graph.getFaces())
			aFaces.add((AFace) new AFace(f,this).setId(f.getId()));

		LOGGER.info("   Build spatial index for units");
		SpatialIndex spUnit = new STRtree();
		for(AUnit u : aUnits) spUnit.insert(u.getObject().getGeom().getEnvelopeInternal(), u);

		LOGGER.info("   Link face and unit agents");
		//for each face, find unit that intersects and make link
		for(AFace aFace : aFaces){
			Polygon faceGeom = aFace.getObject().getGeometry();
			List<AUnit> us = spUnit.query(faceGeom.getEnvelopeInternal());
			for(AUnit u : us) {
				Geometry uGeom = u.getObject().getGeom();
				if(!uGeom.getEnvelopeInternal().intersects(faceGeom.getEnvelopeInternal())) continue;
				if(!uGeom.covers(faceGeom)) continue;
				//link
				aFace.aUnit = u; u.aFaces.add(aFace);
				break;
			}
		}
		return this;
	}



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
		exportFacesAsSHP(outPath, "faces.shp", epsg);
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
				LOGGER.warn("Null geom for unit "+u.getId()+". Nb faces="+u.aFaces.size());
				continue;
			}
			if(f.getGeom().isEmpty()){
				LOGGER.warn("Empty geom for unit "+u.getId()+". Nb faces="+u.aFaces.size());
				continue;
			}
			if(!f.getGeom().isValid()) {
				LOGGER.warn("Non valid geometry for unit "+u.getId()+". Nb faces="+u.aFaces.size());
			}
			f.setProjCode(epsg);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

	public void exportFacesAsSHP(String outPath, String outFile, int epsg) {
		HashSet<Feature> fs = new HashSet<Feature>();
		for(AFace aFace : aFaces) {
			if(aFace.isDeleted()) continue;
			Feature f = aFace.getObject().toFeature();
			if(f.getGeom()==null){
				LOGGER.error("Null geom for face "+aFace.getId()+". Nb edges="+aFace.getObject().getEdges().size());
				continue;
			}
			if(f.getGeom().isEmpty()){
				LOGGER.error("Empty geom for unit "+aFace.getId()+". Nb edges="+aFace.getObject().getEdges().size());
				continue;
			}
			if(!f.getGeom().isValid()) {
				LOGGER.error("Non valid geometry for face "+aFace.getId()+". Nb edges="+aFace.getObject().getEdges().size());
			}
			f.setProjCode(epsg);
			//add unit's id
			f.getProperties().put("unit", aFace.aUnit!=null?aFace.aUnit.getId():null);
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
