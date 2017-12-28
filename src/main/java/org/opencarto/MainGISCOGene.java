/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;
import org.opencarto.util.JTSGeomUtil;

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
	//ogr2ogr -overwrite -f "ESRI Shapefile" "GAUL_CLEAN_DICE_DISSOLVE_WM.shp" "GAUL_CLEAN_DICE_DISSOLVE.shp" -t_srs EPSG:3857 -s_srs EPSG:4258
	//ogr2ogr -overwrite -f "ESRI Shapefile" "EEZ_RG_100K_2013_WM.shp" "EEZ_RG_100K_2013.shp" -t_srs EPSG:3857 -s_srs EPSG:4258

	//0.1mm: 1:1M -> 100m
	//0.1mm: 1:100k -> 10m
	static double resolution1M = 200;

	public static void main(String[] args) {
		LOGGER.info("Start");

		//TODO do global stuff
		//mute too verbose loggers
		//MainGISCOGene.LOGGER.setLevel(Level.WARN);
		GraphBuilder.LOGGER.setLevel(Level.WARN);
		DefaultTesselationGeneralisation.LOGGER.setLevel(Level.WARN);
		ATesselation.LOGGER.setLevel(Level.WARN);

		//check parameters
		//check result
		//fix gaul 1M colombia/brazil

		//simplify reporting model
		//stronger removal of small island/holes?
		//reactivate Face scaling, taking into account frozen edges

		//TODO bosphore straith + dardanelle + bosnia etc. handling
		//TODO handle points labels. capital cities inside countries for all scales

		//TODO check doc of valid and simple checks
		//TODO edge size constraint: fix it!
		//TODO improve evaluation
		//TODO gene for web mapping applications
		//TODO generate label points + separators + join + BN + coastline
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
		final double res = 0.1*resolution1M;
		LOGGER.info("Load data");
		//final int epsg = 3035; String rep="100k/comm"; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"comm_2013/COMM_RG_100k_2013_LAEA.shp", epsg).fs;
		final int epsg = 3857; String rep="100k/gaul"; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"gaul/GAUL_CLEAN_DICE_DISSOLVE_WM.shp", epsg).fs;
		//final int epsg = 3857; String rep="100k_1M/eez"; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath+"eez/EEZ_RG_100K_2013_WM.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");

		Collection<Feature> fs_ = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/","Z_in_"+p.getCode()+".shp");
				MorphologicalAnalysis.removeNarrowGapsTesselation(p.getFeatures(), 1.3*res, 0.5*res*res, 5, 1e-5);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/", "Z_out_"+p.getCode()+".shp");
			}}, fs, 5000000, 25000);
		LOGGER.info("Save");
		for(Feature f : fs_) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs_, outPath+ rep+"/", "out_narrow_gaps_removed.shp");
*/



		//generalisation
		final double res = 0.1*resolution1M;
		LOGGER.info("Load data");
		//final int epsg = 3035; final String rep="100k_1M/comm";
		final int epsg = 3857; final String rep="100k/gaul";
		//final int epsg = 3857; final String rep="100k_1M/eez";
		ArrayList<Feature> fs = SHPUtil.loadSHP(outPath+ rep+"/out_narrow_gaps_removed.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");
		Collection<Feature> fs_ = Partition.runRecursively(new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);
				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/","Z_in_"+p.getCode()+".shp");

				ATesselation t = new ATesselation(p.getFeatures(), p.getEnvelope()); //p.getEnvelope()
				//t.buildTopologicalMap();
				//t.exportFacesAsSHP(outPath+ rep+"/", "out_faces_"+p.getCode()+".shp", epsg);

				for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);
				try {
					DefaultTesselationGeneralisation.run(t, null, res, outPath+ rep);
				} catch (Exception e) { e.printStackTrace(); }
				p.features = t.getUnits(epsg);

				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/", "Z_out_"+p.getCode()+".shp");
			}}, fs, 5000000, 25000);
		for(Feature f : fs_) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs_, outPath+ rep+"/", "out.shp");


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
