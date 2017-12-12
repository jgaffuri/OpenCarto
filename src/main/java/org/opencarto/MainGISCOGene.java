/**
 * 
 */
package org.opencarto;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitNarrowPartsAndGapsXXX;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisationSpecifications;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOGene {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGene.class.getName());
	//-Xmx13g -Xms2g -XX:-UseGCOverheadLimit
	//-XX:-UseGCOverheadLimit
	//-XX:+UseG1GC -XX:G1HeapRegionSize=n -XX:MaxGCPauseMillis=m  
	//-XX:ParallelGCThreads=n -XX:ConcGCThreads=n

	//projs=("etrs89 4258" "wm 3857" "laea 3035")
	//ogr2ogr -overwrite -f "ESRI Shapefile" "t.shp" "s.shp" -t_srs EPSG:3857 -s_srs EPSG:4258
	//ogr2ogr -overwrite -f "ESRI Shapefile" "GAUL_CLEAN_WM.shp" "GAUL_CLEAN.shp" -t_srs EPSG:3857 -s_srs EPSG:4258

	//0.1mm: 1:1M -> 100m
	//0.1mm: 1:100k -> 10m
	static double resolution1M = 200;

	public static void main(String[] args) {
		LOGGER.info("Start");

		//TODO partitionning: solve cell border artefact. Test again cell border addition to linemerger?
		//TODO no removal of small island?

		//TODO bosphore straith + dardanelle + bosnia etc. handling
		//TODO remove larger holes after gap/narrowparts removal
		//TODO handle points labels. capital cities inside countries for all scales

		//TODO topology checker on gaul


		//TODO check doc of valid and simple checks
		//TODO edge size constraint: fix it!
		//TODO improve evaluation
		//TODO use more logger in low level classes to ensure consistency
		//TODO evaluation: ensure partition remains a true partition
		//TODO evaluation: include also straits detection
		//TODO straits: see to ensure all lower resolutions are considered...
		//TODO gene for web mapping applications
		//TODO generate label points + separators + join + BN + coastline
		//TODO keep bosphore and dardanelles open
		//TODO in graph: connect polygon geometry coordinates to edge & node coordinates?
		//TODO replace islands with ellipse?
		//TODO archipelagos detection
		//TODO face collapse algorithm
		//TODO update to log4j 2?
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO use JTS.smooth algorithms?

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";



		/*/narrow gaps removal
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_1M.shp", epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_100k.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"gaul/GAUL_CLEAN_WM.shp", epsg).fs;
		final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"comm_2013/COMM_RG_100k_2013_LAEA.shp", epsg).fs;
		//final int epsg = 3857; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"commplus_100k/COMMPLUS_0404_WM.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");

		fs.sort(new Comparator<Feature>() {
			public int compare(Feature f1, Feature f2) { return f1.id.compareTo(f2.id); }
		});

		Collection<Feature> fs_ = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ "100k_1M/comm/","Z_in_"+p.getCode()+".shp");
				MorphologicalAnalysis.removeNarrowGapsTesselation(p.getFeatures(), 1.3*resolution1M, 0.5*resolution1M*resolution1M, 5);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ "100k_1M/comm/", "Z_out_"+p.getCode()+".shp");
			}}, fs, 2500000, 50000);
		SHPUtil.saveSHP(fs_, outPath+ "100k_1M/comm/", "out_narrow_gaps_removed.shp");
		//SHPUtil.saveSHP(fs_, outPath+ "100k_1M/gaul/", "out_narrow_gaps_removed.shp");
		//SHPUtil.saveSHP(fs_, outPath+ "test/", "out_narrow_gaps_removed.shp");
		 */



		//generalisation (partitionned)
		LOGGER.info("Load data");
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(outPath+ "test/out_narrow_gaps_removed.shp", epsg).fs;
		//final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"comm_2013/COMM_RG_100k_2013_LAEA.shp", epsg).fs;
		final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(outPath+ "100k_1M/comm/out_narrow_gaps_removed_noded.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
		Collection<Feature> fs_ = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				SHPUtil.saveSHP(p.getFeatures(), outPath+ "100k_1M/comm/","Z_in_"+p.getCode()+".shp");

				ATesselation t = new ATesselation(p.getFeatures(), null); //p.getExtend()
				//t.buildTopologicalMap();
				//t.exportFacesAsSHP(outPath+ "100k_1M/comm/", "out_faces_"+p.getCode()+".shp", epsg);

				for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);
				DefaultTesselationGeneralisation.run(t, communesFrom100kSpecs, resolution1M, outPath+ "100k_1M/comm/");
				p.features = t.getUnits(epsg);

				SHPUtil.saveSHP(p.getFeatures(), outPath+ "100k_1M/comm/", "Z_out_"+p.getCode()+".shp");
			}}, fs, 1500000, 25000);
		SHPUtil.saveSHP(fs_, outPath+ "100k_1M/comm/", "out.shp");
		//SHPUtil.saveSHP(fs_, outPath+ "test/", "out.shp");








		/*/nuts regions generalisation
		for(String inputScale : new String[]{"1M"}){
			String inputDataPath = basePath+ "nuts_2013/RG_LAEA_"+inputScale+".shp";
			String straitDataPath = basePath + "out/straits_with_input_"+inputScale+"/straits_";
			for(int targetScaleM : new int[]{1,3,10,20,60}){
				LOGGER.info("--- NUTS generalisation from "+inputScale+" to "+targetScaleM+"M");
				runGeneralisation(inputDataPath, straitDataPath+targetScaleM+"M.shp", NUTSFrom1MSpecs, 3035, targetScaleM*resolution1M, outPath+inputScale+"_input/"+targetScaleM+"M/");
			}
		}*/

		/*/communes generalisation
		for(String inputScale : new String[]{"100k"}){
			String inputDataPathComm = basePath+"comm_2013/COMM_RG_"+inputScale+"_2013_LAEA.shp";
			runNUTSGeneralisation(inputDataPathComm, null, communesFrom100kSpecs, 3035, resolution1M, outPath+"comm_with_input_"+inputScale+"/");
		}*/
		/*/commune 100k extracts
		for(String commDS : new String[]{"finland","france","germany","london","slovenia","spain"}){ //isgreenland
			LOGGER.info("--- COMM generalisation "+commDS);
			String inputDataPathComm = basePath+"comm_2013/extract/COMM_RG_100k_2013_LAEA_"+commDS+".shp";
			runGeneralisation(inputDataPathComm, null, communesFrom100kSpecs, 3035, resolution1M, outPath+"comm_100k_extract/"+commDS+"/");
		}*/





		/*/straits detections
		for(String inputScale : new String[]{"1M","100k"}){
			for(int scaleM : new int[]{1,3,10,20,60}){
				double resolution = scaleM*resolution1M;
				LOGGER.info("--- Straits detection ("+inputScale+" -> "+scaleM+"M, resolution="+resolution+"m)");

				LOGGER.info("Load data");
				ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_"+inputScale+".shp", 3035).fs;
				for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

				LOGGER.info("Run straits detection");
				Collection<Feature> fsOut = MorphologicalAnalysis.runStraitAndBaysDetection(fs, resolution , 1.0 * resolution*resolution, 4);

				LOGGER.info("Save");
				for(Feature f:fsOut) f.setProjCode(3035);
				SHPUtil.saveSHP(fsOut, outPath+"straits_with_input_"+inputScale+"/", "straits_"+scaleM+"M.shp");
			}
		}*/

		/*/narrow parts and gaps (NPG) detection
		for(String inputScale : new String[]{"1M","100k"}){
			for(int scaleM : new int[]{1,3,10,20,60}){
				double resolution = scaleM*resolution1M;
				LOGGER.info("--- NPG detection ("+inputScale+" -> "+scaleM+"M, resolution="+resolution+"m)");

				LOGGER.info("Load data");
				ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+ "nuts_2013/RG_LAEA_"+inputScale+".shp", 3035).fs;
				for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

				LOGGER.info("Run NPG detection");
				Collection<Feature> fsOut = MorphologicalAnalysis.getNarrowPartsAndGaps(fs, resolution , 0.5 * resolution*resolution, 4);

				LOGGER.info("Save");
				for(Feature f:fsOut) f.setProjCode(3035);
				SHPUtil.saveSHP(fsOut, outPath+"NPG_with_input_"+inputScale+"/", "NPG_"+scaleM+"M.shp");
			}
		}*/

		/*static void runNUTSGeneralisationEvaluation(String inputDataPath, int epsg, double resolution, String outPath) {
				new File(outPath).mkdirs();

				LOGGER.info("Load data");
				ArrayList<Feature> fs = SHPUtil.loadSHP(inputDataPath,epsg).fs;
				for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

				LOGGER.info("Create tesselation");
				ATesselation t = new ATesselation(fs);
				fs = null;
				for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

				//TODO run straigths detection

				LOGGER.info("create tesselation's topological map");
				t.buildTopologicalMap();

				LOGGER.info("Set generalisation constraints");
				DefaultTesselationGeneralisation.defaultSpecs.setTopologicalConstraints(t, resolution);
				DefaultTesselationGeneralisation.defaultSpecs.setUnitConstraints(t, resolution); //TODO check that

				//LOGGER.info("Remove generalisation constraint on face size");
				for(AFace af : t.aFaces) af.removeConstraint(af.getConstraint(CFaceSize.class));
				LOGGER.info("Add constraint on unit's size");
				HashMap<String, Double> nutsAreas = loadNutsArea100k();
				for(AUnit au : t.aUnits){
					Double area = nutsAreas.get(au.getId());
					if(area==null) {
						//System.err.println("Could not find area value for nuts "+id);
						continue;
					}
					//LOGGER.info(id+" "+area);
					au.addConstraint(new CUnitSizePreservation(au, area.doubleValue()));
				}

				LOGGER.info("Run evaluation");
				DefaultTesselationGeneralisation.runEvaluation(t, outPath, 7);
			}


			public static HashMap<String,Double> loadNutsArea100k(){
				String inputPath = "/home/juju/Bureau/nuts_gene_data/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
				ArrayList<Feature> fs = SHPUtil.loadSHP(inputPath,3035).fs;
				HashMap<String,Double> out = new HashMap<String,Double>();
				for(Feature f : fs)
					out.put(""+f.getProperties().get("NUTS_ID"), f.getGeom().getArea());
				return out;
			}*/

		//evaluation
		/*/GISCOgene
		for(String inputScale : new String[]{"1M","100k"})
			for(int targetScaleM : new int[]{1,3,10,20,60}) {
				LOGGER.info("--- Evaluation: NUTS generalisation "+inputScale+"-"+targetScaleM+"M");
				runNUTSGeneralisationEvaluation(outPath+inputScale+"_input/"+targetScaleM+"M/units.shp", 3035, targetScaleM*resolution1M, outPath+inputScale+"_input/"+targetScaleM+"M/");
			}
		//1spatial
		for(int targetScaleM : new int[]{3,20,60}) {
			LOGGER.info("--- Evaluation: NUTS/1spatial generalisation "+targetScaleM+"M");
			runNUTSGeneralisationEvaluation(basePath+"1spatial/1Generalise_Result"+targetScaleM+"M.shp", 3857, targetScaleM*resolution1M, basePath+"1spatial/eval"+targetScaleM+"M/");
		}
		//2013 versions
		for(int targetScaleM : new int[]{1,3,10,20,60}) {
			LOGGER.info("--- Evaluation: NUTS 2010 generalisation "+targetScaleM+"M");
			runNUTSGeneralisationEvaluation(basePath+"/nuts_2013/"+targetScaleM+"M/LAEA/lvl3/RG.shp", 3035, targetScaleM*resolution1M, basePath+"/nuts_2013/"+targetScaleM+"M/LAEA/lvl3/");
		}*/
		//TODO comm 1M/100k?
		//TODO nuts 2010 100k too?

		LOGGER.info("End");
	}


	static void runGeneralisation(String inputDataPath, String straitDataPath, TesselationGeneralisationSpecifications specs, int epsg, double resolution, String outPath) {
		new File(outPath).mkdirs();

		LOGGER.info("Load data");
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputDataPath,epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null)
				f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null)
				f.id = ""+f.getProperties().get("COMM_ID");

		LOGGER.info("Create tesselation object");
		ATesselation t = new ATesselation(fs);
		fs = null;
		for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

		if(straitDataPath != null){
			LOGGER.info("Load straits and link them to units");
			//index units by id
			HashMap<String,AUnit> aUnitsI = new HashMap<String,AUnit>();
			for(AUnit au : t.aUnits) aUnitsI.put(au.getId(), au);
			//load straights and link them to units
			ArrayList<Feature> straits = SHPUtil.loadSHP(straitDataPath,epsg).fs;
			for(Feature s : straits){
				AUnit au = aUnitsI.get(s.getProperties().get("unit_id"));
				Collection<Geometry> polys = JTSGeomUtil.getGeometries(s.getGeom());
				if(au.narrowGaps == null) au.narrowGaps = new ArrayList<Polygon>();
				for(Geometry poly : polys) au.narrowGaps.add((Polygon) poly);
				//TODO all polygons?
			}
		}

		LOGGER.info("Run generalisation");
		DefaultTesselationGeneralisation.run(t, specs, resolution, outPath);

		LOGGER.info("Save output");
		t.exportAsSHP(outPath, epsg);
		LOGGER.info("Save report on agents satisfaction");
		t.exportAgentReport(outPath);
	}



	//NUTS specs
	static TesselationGeneralisationSpecifications NUTSFrom1MSpecs = new TesselationGeneralisationSpecifications() {
		public void setUnitConstraints(ATesselation t, double resolution){
			double resSqu = resolution*resolution;
			for(AUnit a : t.aUnits) {
				a.addConstraint(new CUnitNarrowPartsAndGapsXXX(a).setPriority(10));
				//a.addConstraint(new CUnitNoNarrowGaps(a, resolution, 0.1*resSqu, 4).setPriority(10));
				//a.addConstraint(new ConstraintOneShot<AUnit>(a, new TUnitNarrowGapsFilling(a, resolution, 0.1*resSqu, 4)).setPriority(10));
			}
		}

		public void setTopologicalConstraints(ATesselation t, double resolution){
			double resSqu = resolution*resolution;
			for(AFace a : t.aFaces) {
				a.addConstraint(new CFaceSize(a, resSqu*0.7, resSqu, resSqu).setPriority(2));
				a.addConstraint(new CFaceValidity(a).setPriority(1));
				//a.addConstraint(new CFaceNoSmallHoles(a, resSqu*5).setPriority(3));
				//a.addConstraint(new CFaceNoEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()).setPriority(1));
			}
			for(AEdge a : t.aEdges) {
				a.addConstraint(new CEdgeGranularity(a, resolution, true));
				a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
				a.addConstraint(new CEdgeValidity(a));
				a.addConstraint(new CEdgeTriangle(a));
				//a.addConstraint(new CEdgeSize(a, resolution, resolution*0.6));
				//a.addConstraint(new CEdgeNoSelfIntersection(a));
				//a.addConstraint(new CEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()));
			}
		}
	};


	//communes specs
	static TesselationGeneralisationSpecifications communesFrom100kSpecs = new TesselationGeneralisationSpecifications() {
		public void setUnitConstraints(ATesselation t, double resolution){
			double resSqu = resolution*resolution;
			for(AUnit a : t.aUnits) {
				//a.addConstraint(new CUnitNoNarrowPartsAndGapsXXX(a).setPriority(10));
				//a.addConstraint(new CUnitNoNarrowGaps(a, resolution, 0.1*resSqu, 4).setPriority(10));
				//a.addConstraint(new ConstraintOneShot<AUnit>(a, new TUnitNarrowGapsFilling(a, resolution, 0.1*resSqu, 4)).setPriority(10));
			}
		}

		public void setTopologicalConstraints(ATesselation t, double resolution){
			double resSqu = resolution*resolution;
			for(AFace a : t.aFaces) {
				a.addConstraint(new CFaceSize(a, resSqu*0.7, resSqu, resSqu).setPriority(2));
				a.addConstraint(new CFaceValidity(a).setPriority(1));
				//a.addConstraint(new CFaceNoSmallHoles(a, resSqu*5).setPriority(3));
				//a.addConstraint(new CFaceNoEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()).setPriority(1));
			}
			for(AEdge a : t.aEdges) {
				a.addConstraint(new CEdgeGranularity(a, resolution, true));
				a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
				a.addConstraint(new CEdgeValidity(a));
				a.addConstraint(new CEdgeTriangle(a));
				//a.addConstraint(new CEdgeSize(a, resolution, resolution*0.6));
				//a.addConstraint(new CEdgeNoSelfIntersection(a));
				//a.addConstraint(new CEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()));
			}
		}
	};






	/*static void runNUTSGeneralisationEvaluation(String inputDataPath, int epsg, double resolution, String outPath) {
		new File(outPath).mkdirs();

		LOGGER.info("Load data");
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputDataPath,epsg).fs;
		for(Feature f : fs) f.id = ""+f.getProperties().get("NUTS_ID");

		LOGGER.info("Create tesselation");
		ATesselation t = new ATesselation(fs);
		fs = null;
		for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

		//TODO run straigths detection

		LOGGER.info("create tesselation's topological map");
		t.buildTopologicalMap();

		LOGGER.info("Set generalisation constraints");
		DefaultTesselationGeneralisation.defaultSpecs.setTopologicalConstraints(t, resolution);
		DefaultTesselationGeneralisation.defaultSpecs.setUnitConstraints(t, resolution); //TODO check that

		//LOGGER.info("Remove generalisation constraint on face size");
		for(AFace af : t.aFaces) af.removeConstraint(af.getConstraint(CFaceSize.class));
		LOGGER.info("Add constraint on unit's size");
		HashMap<String, Double> nutsAreas = loadNutsArea100k();
		for(AUnit au : t.aUnits){
			Double area = nutsAreas.get(au.getId());
			if(area==null) {
				//System.err.println("Could not find area value for nuts "+id);
				continue;
			}
			//LOGGER.info(id+" "+area);
			au.addConstraint(new CUnitSizePreservation(au, area.doubleValue()));
		}

		LOGGER.info("Run evaluation");
		DefaultTesselationGeneralisation.runEvaluation(t, outPath, 7);
	}


	public static HashMap<String,Double> loadNutsArea100k(){
		String inputPath = "/home/juju/Bureau/nuts_gene_data/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputPath,3035).fs;
		HashMap<String,Double> out = new HashMap<String,Double>();
		for(Feature f : fs)
			out.put(""+f.getProperties().get("NUTS_ID"), f.getGeom().getArea());
		return out;
	}*/


}
