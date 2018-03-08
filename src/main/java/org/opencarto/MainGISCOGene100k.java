/**
 * 
 */
package org.opencarto;

import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;

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

		GraphBuilder.LOGGER.setLevel(Level.WARN);

		//improve testing results. Make more test cases based on incremental validation.
		//removal of large elongated faces/holes: face size constraint: take into account shape - use erosion? use width evaluation method?
		// + do not delete small isolated elements (detect them based on spatial index) - scale them only

		//gene to xM scales

		//TODO bosphore straith + dardanelle + bosnia etc. handling
		//TODO handle points labels. capital cities inside countries for all scales
		//TODO generate label points + separators + join + BN + coastline
		//TODO edge size constraint: fix it!

		//TODO gene for web mapping applications
		//TODO in graph: connect polygon geometry coordinates to edge & node coordinates?
		//TODO archipelagos detection
		//TODO face collapse algorithm
		//TODO update to log4j 2?
		//TODO make graph elements features? link agents to feature (and not object)? Merge feature and agent?
		//TODO use JTS.smooth algorithms?

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		LOGGER.info("Load data");
		//final int epsg = 3035; final String rep="test"; String inFile = basePath+"test/test2.shp";
		final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"commplus/COMM_PLUS_WM.shp";
		Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
		for(Feature f : units) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

		for(int i=1; i<=4; i++) {
			LOGGER.info("Launch generalisation " + i);
			units = DefaultTesselationGeneralisation.runGeneralisation(units, 1e6, 1, false);

			LOGGER.info("Run GC");
			System.gc();

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath+"out/"+ rep+"/", "COMM_PLUS_WM_1M_"+i+".shp");
		}

		LOGGER.info("End");
	}

}
