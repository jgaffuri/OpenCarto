/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Engine;
import org.opencarto.transfoengine.Engine.Stats;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoSelfIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeToEdgeIntersection;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceNoSmallHoles;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeneGISCO {
	//-Xmx13g -Xms2g -XX:-UseGCOverheadLimit

	//0.1mm: 1:1M -> 100m
	static double resolution1M = 200;

	public static void main(String[] args) {
		System.out.println("Start");

		//TODO fix gaussian smoothing: handle closed lines + fix bug with mod. enlarge closed lines?
		//TODO straits detection: improve - for speed etc. fix for 100k-60M
		//TODO test again for COMM generalisation 1M->1M and 100k->1M
		//TODO fix aggregation
		//TODO fix CEdgeMinimumSize and edge collapse
		//TODO gene evaluation - pb detection. run it on 2010 datasets + 1spatial results
		//TODO log process
		//TODO improve activation strategy
		//TODO replace islands with ellipse?

		//TODO propose also amalgamation for enclaves with narrow corridor
		//TODO archipelagos detection
		//TODO face collapse
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?

		/*
		//TODO upgrade JTS and test new simplification algo
<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.14.0</version>
</dependency>
		 */

		String base = "/home/juju/Bureau/nuts_gene_data/";
		String inputDataPath1M = base+ "/nuts_2013/1M/LAEA/lvl3/RG.shp";
		String inputDataPath100k = base+ "/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		String outPath = base+"out/";


		//String inputScale = "1M";
		String inputScale = "100k";
		String inputDataPath = inputScale.equals("1M")? inputDataPath1M : inputDataPath100k;
		String straitDataPath = base + "/out/straits_with_input_"+inputScale+"/straits_";

		/*/nuts regions generalisation
		for(int targetScaleM : new int[]{1,3,10,20,60}){
			System.out.println("--- NUTS generalisation for "+targetScaleM+"M");
			runNUTSGeneralisation(inputDataPath, straitDataPath+targetScaleM+"M.shp", 3035, targetScaleM*resolution1M, outPath+inputScale+"_input/"+targetScaleM+"M/");
		}*/

		//communes generalisation
		String inputDataPathComm = base+"comm_2013/COMM_RG_"+inputScale+"_2013_LAEA.shp";
		runNUTSGeneralisation(inputDataPathComm, null, 3035, resolution1M, outPath+"comm_with_input_"+inputScale+"/");



		/*/straits analysis
		for(int scaleM : new int[]{1,3,10,20,60}){
			double resolution = scaleM*resolution1M;
			System.out.println("--- Straits detection ("+inputScale+" -> "+scaleM+"M, resolution="+resolution+"m)");

			System.out.println("Load data");
			ArrayList<Feature> fs = SHPUtil.loadSHP("100k".equals(inputScale)?inputDataPath100k:inputDataPath1M, 3035).fs;
			for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

			System.out.println("Run straits detection");
			Collection<Feature> fsOut = MorphologicalAnalysis.runStraitAndBaysDetection(fs, resolution , 1.0 * resolution*resolution, 4);

			System.out.println("Save");
			for(Feature f:fsOut) f.setProjCode(3035);
			SHPUtil.saveSHP(fsOut, outPath+"straits_with_input_"+inputScale+"/", "straits_"+scaleM+"M.shp");
		}*/

		System.out.println("End");
	}



	static void runNUTSGeneralisation(String inputDataPath, String straitDataPath, int epsg, double resolution, String outPath) {

		System.out.println("Load data");
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputDataPath,epsg).fs;
		for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

		System.out.println("Create tesselation");
		ATesselation t = new ATesselation(fs);
		fs = null;
		for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

		if(straitDataPath != null){
			System.out.println("Load straits and link them to units");
			ArrayList<Feature> straits = SHPUtil.loadSHP(straitDataPath,epsg).fs;
			HashMap<String,AUnit> aUnitsI = new HashMap<String,AUnit>();
			for(AUnit au : t.aUnits) aUnitsI.put(au.getId(), au);
			for(Feature s : straits){
				AUnit au = aUnitsI.get(s.getProperties().get("unit_id"));
				Collection<Geometry> polys = JTSGeomUtil.getGeometries(s.getGeom());
				for(Geometry poly : polys) au.straits.add((Polygon) poly);
			}
			aUnitsI = null; straits = null;

			System.out.println("Handle straits");
			for(AUnit au : t.aUnits){
				try {
					au.absorbStraits();
				} catch (Exception e) {
					System.err.println("Failed absorbing straits for "+au.getId() + "  "+e.getMessage());
					//e.printStackTrace();
				}
			}
		}


		System.out.println("create tesselation's topological map");
		t.buildTopologicalMap();

		System.out.println("Add graph generalisation constraints");
		double resSqu = resolution*resolution;
		for(AEdge edgAg : t.aEdges){
			edgAg.addConstraint(new CEdgeNoSelfIntersection(edgAg));
			edgAg.addConstraint(new CEdgeToEdgeIntersection(edgAg, t.graph.getSpatialIndexEdge()));
			edgAg.addConstraint(new CEdgeGranularity(edgAg, resolution, true)); //TODO should be something more like shape complexity + add
			edgAg.addConstraint(new CEdgeNoTriangle(edgAg));
			//edgAg.addConstraint(new CEdgeMinimumSize(edgAg, resolution*0.8, resolution));
		}
		for(AFace faceAg : t.aFaces){
			faceAg.addConstraint(new CFaceNoSmallHoles(faceAg, resSqu*2));
			faceAg.addConstraint(new CFaceSize(faceAg, resSqu*0.7, resSqu));
		}


		//t.exportFacesAsSHP(outPath, "faces_input.shp", epsg);
		//t.exportEdgesAsSHP(outPath, "edge_input.shp", epsg);


		//engines
		Engine<AFace> fEng = new Engine<AFace>(t.aFaces);
		Engine<AEdge> eEng = new Engine<AEdge>(t.aEdges);

		//TODO include that in engine
		System.out.println("Compute initial satisfaction");
		Stats dStatsIni = fEng.getSatisfactionStats();
		Stats eStatsIni = eEng.getSatisfactionStats();

		System.out.println("Run generalisation");
		System.out.println("   faces 1");
		fEng.activateQueue();
		System.out.println("   edges 1");
		eEng.activateQueue();
		System.out.println("   faces 2");
		fEng.activateQueue();
		System.out.println("   edges 1");
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
		t.exportAsSHP(outPath, epsg);
		System.out.println("Save report on agents satisfaction");
		t.exportAgentReport(outPath);
	}

}
