/**
 * 
 */
package org.opencarto;

import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;
import org.opencarto.util.JTSGeomUtil;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOGene100k {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGene100k.class.getName());
	//-Xmx13g -Xms2g -XX:-UseGCOverheadLimit
	//-XX:-UseGCOverheadLimit
	//-XX:+UseG1GC -XX:G1HeapRegionSize=n -XX:MaxGCPauseMillis=m  
	//-XX:ParallelGCThreads=n -XX:ConcGCThreads=n

	//projs=("etrs89 4258" "wm 3857" "laea 3035")
	//ogr2ogr -overwrite -f "ESRI Shapefile" "t.shp" "s.shp" -t_srs EPSG:3857 -s_srs EPSG:4258
	//ogr2ogr -overwrite -f "ESRI Shapefile" "GAUL_CLEAN_DICE_DISSOLVE_WM.shp" "GAUL_CLEAN_DICE_DISSOLVE.shp" -t_srs EPSG:3857 -s_srs EPSG:4258
	//ogr2ogr -overwrite -f "ESRI Shapefile" "EEZ_RG_100K_2013_WM.shp" "EEZ_RG_100K_2013.shp" -t_srs EPSG:3857 -s_srs EPSG:4258

	public static void main(String[] args) {
		LOGGER.info("Start");

		//TODO do global stuff
		//mute too verbose loggers
		//MainGISCOGene.LOGGER.setLevel(Level.WARN);
		GraphBuilder.LOGGER.setLevel(Level.WARN);
		DefaultTesselationGeneralisation.LOGGER.setLevel(Level.WARN);
		ATesselation.LOGGER.setLevel(Level.WARN);
		//Agent.LOGGER.setLevel(Level.ALL);

		//improve testing results. Make more test cases.
		//removal of large elongated faces/holes: face size constraint: take into account shape - use erosion? use width evaluation method?
		// + do not delete small isolated elements (detect them based on spatial index) - scale them only

		//gene to xM scales

		//TODO bosphore straith + dardanelle + bosnia etc. handling
		//TODO handle points labels. capital cities inside countries for all scales
		//TODO generate label points + separators + join + BN + coastline
		//TODO edge size constraint: fix it!
		//TODO simplify reporting model ?

		//TODO gene for web mapping applications
		//TODO in graph: connect polygon geometry coordinates to edge & node coordinates?
		//TODO archipelagos detection
		//TODO face collapse algorithm
		//TODO update to log4j 2?
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO use JTS.smooth algorithms?

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";

		final CartographicResolution res = new CartographicResolution(1e6);

		Collection<Feature> fs, fs_;

		//narrow gaps removal
		LOGGER.info("Load data");
		//final int epsg = 3035; final String rep="test"; String inFile = basePath+"test/test.shp";
		final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"commplus/COMM_PLUS_WM.shp";

		fs = SHPUtil.loadSHP(inFile, epsg).fs;
		for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);
		fs_ = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/","Z_in_"+p.getCode()+".shp");

				//TODO move that to DefaultTesselationGeneralisation ?
				MorphologicalAnalysis.removeNarrowGapsTesselation(p.getFeatures(), res.getSeparationDistanceMeter(), 5, 1e-5);

				ATesselation t = new ATesselation(p.getFeatures(), p.getEnvelope()); //p.getEnvelope()
				for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

				try {
					DefaultTesselationGeneralisation.run(t, null, res, outPath+ rep);
				} catch (Exception e) { e.printStackTrace(); }
				p.features = t.getUnits(epsg);

				//System.gc();

				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/", "Z_out_"+p.getCode()+".shp");
				//}}, fs, 5000000, 25000);
				//}}, fs, 3000000, 15000, false);
			}}, fs, 1000000, 5000, false);
		for(Feature f : fs_) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs_, outPath+ rep+"/", "out.shp");

		/*

		//narrow gaps removal
		LOGGER.info("Load data");
		//final int epsg = 3035; final String rep="test"; String inFile = basePath+"test/test.shp";
		final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"commplus/COMM_PLUS_100k_WM.shp";

		fs = SHPUtil.loadSHP(inFile, epsg).fs;
		for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);
		fs_ = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/","Z_in_"+p.getCode()+".shp");
				MorphologicalAnalysis.removeNarrowGapsTesselation(p.getFeatures(), res.getSeparationDistanceMeter(), 5, 1e-5);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/", "Z_out_"+p.getCode()+".shp");
				//}}, fs, 5000000, 25000);
			}}, fs, 3000000, 15000, false);
		LOGGER.info("Save");
		for(Feature f : fs_) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs_, outPath+ rep+"/", "out_narrow_gaps_removed.shp");


		//final int epsg = 3857; final String rep="100k_1M/commplus";

		//generalisation
		LOGGER.info("Load data");
		//final int epsg = 3857; final String rep="100k_1M/commplus";
		fs = SHPUtil.loadSHP(outPath+ rep+"/out_narrow_gaps_removed___.shp", epsg).fs;
		for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);
		fs_ = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/part_in/","Z_in_"+p.getCode()+".shp");

				ATesselation t = new ATesselation(p.getFeatures(), p.getEnvelope()); //p.getEnvelope()
				for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

				try {
					DefaultTesselationGeneralisation.run(t, null, res, outPath+ rep);
				} catch (Exception e) { e.printStackTrace(); }
				p.features = t.getUnits(epsg);

				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/", "Z_out_"+p.getCode()+".shp");
				//}}, fs, 5000000, 25000);
			}}, fs, 3000000, 15000, false);
		for(Feature f : fs_) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs_, outPath+ rep+"/", "out___.shp");

		 */		

		LOGGER.info("End");
	}

	/*
	public static HashMap<String,Double> loadNutsArea100k(){
		String inputPath = "/home/juju/Bureau/nuts_gene_data/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputPath,3035).fs;
		HashMap<String,Double> out = new HashMap<String,Double>();
		for(Feature f : fs)
			out.put(""+f.getProperties().get("NUTS_ID"), f.getGeom().getArea());
		return out;
	}*/

}
