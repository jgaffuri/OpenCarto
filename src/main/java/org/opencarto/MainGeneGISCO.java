/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.algo.polygon.MorphologicalAnalysis;
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
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;

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

		//TODO check straits do not intersects each other and do not intersect units
		//TODO include straits - as unit constraint?
		//TODO use smoothing algorithm - gaussian. Design new operation composed of filtering+gaussian
		//TODO test again for COMM generalisation 100k->1M
		//TODO improve activation strategy

		//TODO try all scales one by one - from 1M and from 100k --- fails for 1M-60M and 100k-1M. Could not find aggregation candidate
		/* with 100k source
Error when removing node N72791. Edges are still linked to it (nb=1)
Error when removing node N72792. Edges are still linked to it (nb=1)
Error when removing node N72116. Edges are still linked to it (nb=2)
Error when removing node N72116. Faces are still linked to it (nb=2)
Error when removing node N72872. Edges are still linked to it (nb=1)
Error when removing node N72871. Edges are still linked to it (nb=1)
		 */
		//TODO gene evaluation - pb detection. run it on 2010 datasets + 1spatial results
		//TODO log process
		//TODO archipelagos detection
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO face collapse

		/*
		//TODO upgrade JTS and test new simplification algo
<dependency>
    <groupId>com.vividsolutions</groupId>
    <artifactId>jts-core</artifactId>
    <version>1.14.0</version>
</dependency>
		 */

		String base = "/home/juju/Bureau/nuts_gene_data/";
		String inputDataPath1M = base+"/nuts_2013/1M/LAEA/lvl3/RG.shp";
		String inputDataPath100k = base+"/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		//String inputDataPathCOMM_1M = base+"comm_2013/COMM_RG_01M_2013_LAEA.shp";
		//String inputDataPathCOMM_100k = base+"comm_2013/COMM_RG_100k_2013_LAEA.shp";
		String outPath = base+"out/";



		//runNUTSGeneralisation(inputDataPath1M, 3035, 60*resolution1M, outPath);

		//runNUTSGeneralisationAllScales(inputDataPath1M, 3035, outPath+"1M_input/");
		//runNUTSGeneralisationAllScales(inputDataPath100k, 3035, outPath+"100k_input/");



		//straits analysis
		//String inputScale = "1M";
		String inputScale = "100k";
		for(int scaleM : new int[]{/*1,3,10,20,*/60}){
			double resolution = scaleM*resolution1M;
			System.out.println("--- Straits detection ("+inputScale+" -> "+scaleM+"M, resolution="+resolution+"m)");

			System.out.println("Load data");
			ArrayList<Feature> fs = SHPUtil.loadSHP("100k".equals(inputScale)?inputDataPath100k:inputDataPath1M, 3035).fs;
			for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

			System.out.println("Run strait detection");
			Collection<Feature> fsOut = MorphologicalAnalysis.runStraitAndBaysDetection(fs, resolution , 1.0 * resolution*resolution, 5);

			System.out.println("Save");
			for(Feature f:fsOut) f.setProjCode(3035);
			SHPUtil.saveSHP(fsOut, outPath+"straits_with_input_"+inputScale+"/", "straits_"+scaleM+"M.shp");
		}

		System.out.println("End");
	}



	static void runNUTSGeneralisation(String inputDataPath, int epsg, double resolution, String outPath) {

		System.out.println("Load data");
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputDataPath,epsg).fs;
		for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

		//TODO include straits here?
		//Collection<Feature> fsStraits = StraitDetection.get(fs, resolution, 2*resolution*resolution, 5);
		//TODO include geom of straits to unit's geom

		System.out.println("Build tesselation");
		ATesselation t = new ATesselation(fs);
		fs = null;

		//use nuts_id as identifier
		for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

		System.out.println("Add generalisation constraints");
		double resSqu = resolution*resolution;
		for(AEdge edgAg : t.aEdges){
			edgAg.addConstraint(new CEdgeNoSelfIntersection(edgAg));
			edgAg.addConstraint(new CEdgeToEdgeIntersection(edgAg, t.graph.getSpatialIndexEdge()));
			edgAg.addConstraint(new CEdgeGranularity(edgAg, resolution, true)); //TODO should be something more like shape complexity + add
			edgAg.addConstraint(new CEdgeNoTriangle(edgAg));
			//edgAg.addConstraint(new CEdgeMinimumSize(edgAg, resolution*0.8, resolution));
			//TODO add constraint on edge position?
		}
		for(AFace faceAg : t.aFaces){
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
		fEng.activateQueue();
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


	//generalisation process for all NUTS scales
	static void runNUTSGeneralisationAllScales(String inputDataPath, int epsg, String outPath) {
		//resolutions 0.1mm: 1:1M -> 100m
		for(int scale : new int[]{1,3,10,20,60}){
			System.out.println("--- NUTS generalisation for "+scale+"M");
			runNUTSGeneralisation(inputDataPath, 3035, scale*resolution1M, outPath+scale+"M/");
		}

	}

}
