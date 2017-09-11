/**
 * 
 */
package org.opencarto;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitSizePreservation;
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

		//TODO ensure face size preservation: add edge constraint on that, with high importance
		//TODO ensure 60M edges are simplified - something to fix with edge minimum size?
		//TODO add constraint on unit narrow parts?
		//TODO use more logger in low level classes to ensure consistency
		//TODO update to log4j 2 ?
		//TODO evaluation: ensure partition remains a true partition
		//TODO evaluation: include also straits detection
		//TODO fix CEdgeMinimumSize and edge collapse: move nodes, check polygon validity and if all valids, collapse it.
		//TODO straits: see to ensure all lower resolutions are considered...
		//TODO examine satisfaction values (worst results) and handle it!
		//TODO gaussian smoothing for closed lines. enlarge islands after?
		//TODO straits detection: improve - for speed etc. fix for 100k-60M
		//TODO gene for web mapping applications

		//TODO fix bruxelles case: better defined face size constraint importance
		//TODO narrow patch detection - transfer from face to face. fromUnit,toUnit
		//TODO replace islands with ellipse?
		//TODO keep bosphore and dardanelles open
		//TODO archipelagos detection
		//TODO face collapse
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		String outPath = basePath+"out/";

		//nuts regions generalisation
		for(String inputScale : new String[]{"1M","100k"}){
			String inputDataPath = basePath+ "nuts_2013/RG_LAEA_"+inputScale+".shp";
			String straitDataPath = basePath + "out/straits_with_input_"+inputScale+"/straits_";
			for(int targetScaleM : new int[]{1,3,10,20,60}){
				System.out.println("--- NUTS generalisation from "+inputScale+" to "+targetScaleM+"M");
				runNUTSGeneralisation(inputDataPath, straitDataPath+targetScaleM+"M.shp", 3035, targetScaleM*resolution1M, outPath+inputScale+"_input/"+targetScaleM+"M/");
			}
		}

		/*/communes generalisation
		for(String inputScale : new String[]{"100k"}){
			String inputDataPathComm = base+"comm_2013/COMM_RG_"+inputScale+"_2013_LAEA.shp";
			runNUTSGeneralisation(inputDataPathComm, null, 3035, resolution1M, outPath+"comm_with_input_"+inputScale+"/");
		}*/
		/*/commune 100k extracts
		for(String commDS : new String[]{"finland","london","germany","slovenia","spain","france"}){ //isgreenland
			System.out.println("--- COMM generalisation "+commDS);
			String inputDataPathComm = basePath+"comm_2013/extract/COMM_RG_100k_2013_LAEA_"+commDS+".shp";
			runNUTSGeneralisation(inputDataPathComm, null, 3035, resolution1M, outPath+"comm_100k_extract/"+commDS+"/");
		}*/

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

		//evaluation
		/*/GISCOgene
		for(String inputScale : new String[]{"1M","100k"})
			for(int targetScaleM : new int[]{1,3,10,20,60}) {
				System.out.println("--- Evaluation: NUTS generalisation "+inputScale+"-"+targetScaleM+"M");
				runNUTSGeneralisationEvaluation(outPath+inputScale+"_input/"+targetScaleM+"M/units.shp", 3035, targetScaleM*resolution1M, outPath+inputScale+"_input/"+targetScaleM+"M/");
			}
		//1spatial
		for(int targetScaleM : new int[]{3,20,60}) {
			System.out.println("--- Evaluation: NUTS/1spatial generalisation "+targetScaleM+"M");
			runNUTSGeneralisationEvaluation(basePath+"1spatial/1Generalise_Result"+targetScaleM+"M.shp", 3857, targetScaleM*resolution1M, basePath+"1spatial/eval"+targetScaleM+"M/");
		}
		//2013 versions
		for(int targetScaleM : new int[]{1,3,10,20,60}) {
			System.out.println("--- Evaluation: NUTS 2010 generalisation "+targetScaleM+"M");
			runNUTSGeneralisationEvaluation(basePath+"/nuts_2013/"+targetScaleM+"M/LAEA/lvl3/RG.shp", 3035, targetScaleM*resolution1M, basePath+"/nuts_2013/"+targetScaleM+"M/LAEA/lvl3/");
		}*/
		//TODO comm 1M/100k?
		//TODO nuts 2010 100k too?

		System.out.println("End");
	}


	static void runNUTSGeneralisation(String inputDataPath, String straitDataPath, int epsg, double resolution, String outPath) {
		new File(outPath).mkdirs();

		System.out.println("Load data");
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputDataPath,epsg).fs;
		for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

		System.out.println("Create tesselation");
		ATesselation t = new ATesselation(fs);
		fs = null;
		for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

		if(straitDataPath != null){
			System.out.println("Load straits and link them to units");
			//index units by id
			HashMap<String,AUnit> aUnitsI = new HashMap<String,AUnit>();
			for(AUnit au : t.aUnits) aUnitsI.put(au.getId(), au);
			//load straights and link them to units
			ArrayList<Feature> straits = SHPUtil.loadSHP(straitDataPath,epsg).fs;
			for(Feature s : straits){
				AUnit au = aUnitsI.get(s.getProperties().get("unit_id"));
				Collection<Geometry> polys = JTSGeomUtil.getGeometries(s.getGeom());
				for(Geometry poly : polys) au.straits.add((Polygon) poly);
				//TODO all polygons?
			}
		}

		System.out.println("Run generalisation");
		t.run(resolution, outPath);

		System.out.println("Save output");
		t.exportAsSHP(outPath, epsg);
		System.out.println("Save report on agents satisfaction");
		t.exportAgentReport(outPath);
	}



	static void runNUTSGeneralisationEvaluation(String inputDataPath, int epsg, double resolution, String outPath) {
		new File(outPath).mkdirs();

		System.out.println("Load data");
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputDataPath,epsg).fs;
		for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

		System.out.println("Create tesselation");
		ATesselation t = new ATesselation(fs);
		fs = null;
		for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

		//TODO run straigths detection

		System.out.println("create tesselation's topological map");
		t.buildTopologicalMap();

		System.out.println("Set generalisation constraints");
		t.setTopologicalConstraints(resolution);
		t.setUnitConstraints(resolution); //TODO check that

		//System.out.println("Remove generalisation constraint on face size");
		for(AFace af : t.aFaces) af.removeConstraint(af.getConstraint(CFaceSize.class));
		System.out.println("Add constraint on unit's size");
		HashMap<String, Double> nutsAreas = loadNutsArea100k();
		for(AUnit au : t.aUnits){
			Double area = nutsAreas.get(au.getId());
			if(area==null) {
				//System.err.println("Could not find area value for nuts "+id);
				continue;
			}
			//System.out.println(id+" "+area);
			au.addConstraint(new CUnitSizePreservation(au, area.doubleValue()));
		}

		System.out.println("Run evaluation");
		t.runEvaluation(outPath, 7);

	}


	public static HashMap<String,Double> loadNutsArea100k(){
		String inputPath = "/home/juju/Bureau/nuts_gene_data/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputPath,3035).fs;
		HashMap<String,Double> out = new HashMap<String,Double>();
		for(Feature f : fs)
			out.put(""+f.getProperties().get("NUTS_ID"), f.getGeom().getArea());
		return out;
	}

}
