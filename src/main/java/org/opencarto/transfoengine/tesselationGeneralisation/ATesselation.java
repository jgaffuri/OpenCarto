/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.opencarto.util.JTSGeomUtil;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * A tesselation to be generalised. It is a macro agent.
 * 
 * @author julien Gaffuri
 *
 */
public class ATesselation extends Agent {
	public final static Logger LOGGER = Logger.getLogger(ATesselation.class.getName());

	public Collection<AUnit> aUnits;

	public Graph graph;
	public Collection<AEdge> aEdges;
	public Collection<AFace> aFaces;
	private Envelope env = null;

	public ATesselation(Collection<Feature> units) { this(units, null, null); }
	public ATesselation(Collection<Feature> units, Envelope env, HashMap<String, Collection<Point>> points){
		super(null);

		//create unit agents
		aUnits = new HashSet<AUnit>();
		for(Feature unit : units) {
			//create unit
			AUnit au = new AUnit(unit, this);
			aUnits.add(au);
			//link points to unit
			if(points != null) {
				Collection<Point> pts = points.get(au.getId());
				if(pts != null ) {
					au.points = new ArrayList<Point>();
					au.points.addAll(pts);
				}
			}
		}

		this.env = env;
	}


	//build topological map
	public ATesselation buildTopologicalMap() throws Exception {

		//get unit's boundaries
		Collection<MultiPolygon> mps = new ArrayList<MultiPolygon>();
		for(AUnit au : aUnits)
			mps.add((MultiPolygon) au.getObject().getGeom());

		//build graph
		graph = GraphBuilder.buildForTesselation(mps, env);
		mps.clear(); mps = null;

		//create edge and face agents
		aEdges = new HashSet<AEdge>();
		for(Edge e : graph.getEdges()) {
			AEdge ae = (AEdge) new AEdge(e,this).setId(e.getId());
			if(isToBeFreezed(ae)) ae.freeze();
			aEdges.add(ae);
		}
		aFaces = new HashSet<AFace>();
		for(Face f : graph.getFaces())
			aFaces.add((AFace) new AFace(f,this).setId(f.getId()));

		LOGGER.debug("   Build spatial index for units");
		STRtree spUnit = new STRtree();
		for(AUnit u : aUnits) spUnit.insert(u.getObject().getGeom().getEnvelopeInternal(), u);

		LOGGER.debug("   Link face and unit agents");
		//for each face, find unit that intersects and make link
		for(AFace aFace : aFaces){
			Polygon faceGeom = aFace.getObject().getGeom();
			for(AUnit u : (List<AUnit>)spUnit.query(faceGeom.getEnvelopeInternal())) {
				Geometry uGeom = u.getObject().getGeom();
				if(!uGeom.getEnvelopeInternal().intersects(faceGeom.getEnvelopeInternal())) continue;
				if(!uGeom.covers(faceGeom)) continue;
				//link
				if(u.aFaces == null) u.aFaces = new HashSet<AFace>();
				aFace.aUnit = u; u.aFaces.add(aFace);
				break;
			}
		}

		//link points to faces
		for(AUnit au : aUnits) {
			if(au.points == null) continue;
			au.linkPointsToFaces();
		}

		return this;
	}

	public ATesselation destroyTopologicalMap() {
		if(graph != null) { graph.clear(); graph = null; }
		if(aEdges != null) { for(AEdge a: aEdges) a.clear(); aEdges.clear(); aEdges = null; }
		if(aFaces != null) { for(AFace a: aFaces) a.clear(); aFaces.clear(); aFaces = null; }
		return this;
	}


	public AEdge getAEdge(Edge e){
		if(e == null) return null;
		for(AEdge ae:aEdges) if(ae.getObject()==e) return ae;
		return null;
	}
	public AFace getAFace(Face f){
		if(f == null) return null;
		for(AFace af:aFaces) if(af.getObject()==f) return af;
		return null;
	}
	public AUnit getAUnit(Feature unit) {
		if(unit == null) return null;
		for(AUnit au:aUnits) if(au.getObject()==unit) return au;
		return null;
	}


	public void exportAsSHP(String outPath, CoordinateReferenceSystem crs) {
		exportUnitsAsSHP(outPath+"units.shp", crs);
		exportFacesAsSHP(outPath+"faces.shp", crs);
		exportEdgesAsSHP(outPath+"edges.shp", crs);
		exportNodesAsSHP(outPath+"nodes.shp", crs);
	}



	public void exportUnitsAsSHP(String outFile, CoordinateReferenceSystem crs){
		if(aUnits ==null || aUnits.size()==0) { LOGGER.warn("No units to export for tesselation "+getId()); return; }
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
				LOGGER.warn("Non valid geometry for unit "+u.getId()+". Nb faces="+(u.aFaces!=null?u.aFaces.size():"null"));
			}
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outFile, crs);
	}

	public void exportFacesAsSHP(String outFile, CoordinateReferenceSystem crs) {
		if(aFaces ==null || aFaces.size()==0) { LOGGER.warn("No faces to export for tesselation "+getId()); return; }
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
			//add unit's id
			f.getProperties().put("unit", aFace.aUnit!=null?aFace.aUnit.getId():null);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outFile, crs);
	}

	public void exportEdgesAsSHP(String outFile, CoordinateReferenceSystem crs) {
		if(aEdges ==null || aEdges.size()==0) { LOGGER.warn("No edges to export for tesselation "+getId()); return; }
		HashSet<Feature> fs = new HashSet<Feature>();
		for(AEdge aEdg:aEdges){
			if(aEdg.isDeleted()) continue;
			Feature f = aEdg.getObject().toFeature();
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outFile, crs);
	}

	public void exportNodesAsSHP(String outFile, CoordinateReferenceSystem crs) {
		if(graph == null || graph.getNodes().size()==0) { LOGGER.warn("No faces to export for tesselation "+getId()); return; }
		SHPUtil.saveSHP(graph.getNodeFeatures(), outFile, crs);
	}


	/*
	public Collection<Feature> getUnits(int epsg) {
		Collection<Feature> units = new HashSet<Feature>();
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
				LOGGER.warn("Non valid geometry for unit "+u.getId()+". Nb faces="+(u.aFaces!=null?u.aFaces.size():"null"));
			}
			f.setProjCode(epsg);
			units.add(f);
		}
		return units;
	}*/

	public Collection<Feature> getUnits() {
		Collection<Feature> units = new HashSet<Feature>();
		for(AUnit u : aUnits) {
			if(u.isDeleted()) continue;
			Feature f = u.getObject();
			if(f.getGeom()==null){
				LOGGER.warn("Null geom for unit "+u.getId()+". Nb faces="+(u.aFaces!=null?u.aFaces.size():"null"));
				continue;
			}
			if(f.getGeom().isEmpty()){
				LOGGER.warn("Empty geom for unit "+u.getId()+". Nb faces="+(u.aFaces!=null?u.aFaces.size():"null"));
				continue;
			}
			if(!f.getGeom().isValid()) {
				LOGGER.warn("Non valid geometry for unit "+u.getId()+". Nb faces="+(u.aFaces!=null?u.aFaces.size():"null"));
				f.setGeom( (MultiPolygon)JTSGeomUtil.toMulti(f.getGeom().buffer(0)) );
				LOGGER.warn(f.getGeom().isValid()? " Fixed!" : " Not fixed...");
			}
			units.add(f);
		}
		return units;
	}

	private boolean isToBeFreezed(AEdge ae) {
		if(this.env == null) return false;
		Geometry g = ae.getObject().getGeometry();
		if (JTSGeomUtil.containsSFS(this.env, g.getEnvelopeInternal())) return false;
		double length = g.intersection(JTSGeomUtil.getBoundary(this.env)).getLength();
		if(length == 0) return false;
		else if(length == g.getLength()) return true;
		else {
			LOGGER.warn("*** "+length+" "+g.getLength()+" "+(length-g.getLength()));
			return true;
		}
	}

	public void clear() {
		super.clear();
		destroyTopologicalMap();
		if(aUnits != null) { for(AUnit a: aUnits) a.clear(); aUnits.clear(); aUnits=null; }
	}

	//TODO improve, with spatial index?
	public Collection<AUnit> query(Envelope env) {
		Collection<AUnit> out = new ArrayList<AUnit>();
		for(AUnit au : aUnits) {
			if(au.getObject().getGeom() == null) continue;
			if(au.getObject().getGeom().isEmpty()) continue;
			if(! env.intersects(au.getObject().getGeom().getEnvelopeInternal())) continue;
			out.add(au);
		}
		return out;
	}

}
