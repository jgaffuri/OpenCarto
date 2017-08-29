/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
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

		Logger.getLogger(Agent.class.getName()).setLevel(Level.FINEST);

		//TODO log process

		//TODO fix bugs
		//TODO fix aggregation: merge edges + handle consequences on agents
		//TODO fix CEdgeMinimumSize and edge collapse
		//TODO straits: see to ensure all lower resolutions are considered...
		//TODO examine satisfaction values (worst results) and handle it!
		//TODO gaussian smoothing for closed lines. enlarge islands after?
		//TODO straits detection: improve - for speed etc. fix for 100k-60M
		//TODO gene evaluation - pb detection. run it on 2010 datasets + 1spatial results
		//TODO replace islands with ellipse?
		//TODO gene for web mapping applications

		//TODO narrow patch detection - transfer from face to face. fromUnit,toUnit
		//TODO fix bruxelles case: better defined face size constraint importance
		//TODO keep bosphore and dardanelles open
		//TODO archipelagos detection
		//TODO face collapse
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		String outPath = basePath+"out/";

		//nuts regions generalisation
		String inputDataPath1M = basePath+ "/nuts_2013/1M/LAEA/lvl3/RG.shp";
		String inputDataPath100k = basePath+ "/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		for(String inputScale : new String[]{"1M"}){
			String inputDataPath = inputScale.equals("1M")? inputDataPath1M : inputDataPath100k;
			String straitDataPath = basePath + "/out/straits_with_input_"+inputScale+"/straits_";
			for(int targetScaleM : new int[]{1}){
				System.out.println("--- NUTS generalisation for "+targetScaleM+"M");
				runNUTSGeneralisation(inputDataPath, straitDataPath+targetScaleM+"M.shp", 3035, targetScaleM*resolution1M, outPath+inputScale+"_input/"+targetScaleM+"M/");
			}
		}

		/*/communes generalisation
		for(String inputScale : new String[]{"100k"}){
			String inputDataPathComm = base+"comm_2013/COMM_RG_"+inputScale+"_2013_LAEA.shp";
			runNUTSGeneralisation(inputDataPathComm, null, 3035, resolution1M, outPath+"comm_with_input_"+inputScale+"/");
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

		System.out.println("Run generalisation");
		t.run(resolution);

		System.out.println("Save output");
		t.exportAsSHP(outPath, epsg);
		System.out.println("Save report on agents satisfaction");
		t.exportAgentReport(outPath);
	}

}
