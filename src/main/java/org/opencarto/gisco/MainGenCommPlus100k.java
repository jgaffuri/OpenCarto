/**
 * 
 */
package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;
import org.opencarto.util.ProjectionUtil.CRSType;

/**
 * @author julien Gaffuri
 *
 */
public class MainGenCommPlus100k {
	private final static Logger LOGGER = Logger.getLogger(MainGenCommPlus100k.class.getName());
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

		/*/define specifications
		TesselationGeneralisationSpecification specs = new TesselationGeneralisationSpecification() {
			public void setUnitConstraints(ATesselation t, CartographicResolution res) {
				for(AUnit a : t.aUnits) {
					a.addConstraint(new CUnitNoNarrowGaps(a, res.getSeparationDistanceMeter(), 1e-5, 5, true, true).setPriority(10));
					//a.addConstraint(new CUnitNoNarrowParts(a, res.getSeparationDistanceMeter(), 1e-5, 5, true).setPriority(9));
					a.addConstraint(new CUnitContainPoints(a));
				}
			}
			public void setTopologicalConstraints(ATesselation t, CartographicResolution res) {
				for(AFace a : t.aFaces) {
					a.addConstraint(new CFaceSize(a, 0.1*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), true, true).setPriority(2));
					a.addConstraint(new CFaceValidity(a));
					a.addConstraint(new CFaceContainPoints(a));
					a.addConstraint(new CFaceEEZInLand(a).setPriority(10));
				}
				for(AEdge a : t.aEdges) {
					a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM()));
					a.addConstraint(new CEdgeValidity(a));
					a.addConstraint(new CEdgeNoTriangle(a));
					a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
					a.addConstraint(new CEdgesFacesContainPoints(a));
				}
			}
		};*/


		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		LOGGER.info("Load data");
		//final int epsg = 3035; final String rep="test"; String inFile = basePath+"test/test2.shp";
		final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"commplus/COMM_PLUS_WM.shp";
		//final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"out/"+ rep+"/COMM_PLUS_WM_1M_6.shp";
		Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
		for(Feature f : units) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

		for(int i=1; i<=100; i++) {
			LOGGER.info("Launch generalisation " + i);
			LOGGER.error("FIX !!!");
			units = TesselationGeneralisation.runGeneralisation(units, null, CRSType.CARTO, /*specs*/null, 1e6, 1, false, 1000000, 1000);
			if(true) return;

			LOGGER.info("Run GC");
			System.gc();

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath+"out/"+ rep+"/COMM_PLUS_WM_1M_"+i+".shp");
		}

		LOGGER.info("End");
	}

}
