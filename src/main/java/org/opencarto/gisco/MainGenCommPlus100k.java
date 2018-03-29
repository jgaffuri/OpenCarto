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
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CTesselationMorphology;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisationSpecifications;

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

		//TODO area preservation (gibraltar/san marino)
		//TODO control point in area
		//TODO deployment
		//TODO move narrow gap removal at unit level

		//TODO gene for web mapping applications
		//TODO implement narrow corridor removal
		//TODO removal of large elongated faces/holes: face size constraint: take into account shape - use erosion? use width evaluation method?
		//TODO face collapse algorithm
		//TODO edge size constraint: fix it!
		//TODO in graph: connect polygon geometry coordinates to edge & node coordinates?
		//TODO archipelagos detection

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		LOGGER.info("Load data");
		//final int epsg = 3035; final String rep="test"; String inFile = basePath+"test/test2.shp";
		final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"commplus/COMM_PLUS_WM.shp";
		//final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"out/"+ rep+"/COMM_PLUS_WM_1M_6.shp";
		Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
		for(Feature f : units) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

		for(int i=1; i<=100; i++) {
			LOGGER.info("Launch generalisation " + i);
			units = DefaultTesselationGeneralisation.runGeneralisation(units, null, specs, 1e6, 1, false);

			LOGGER.info("Run GC");
			System.gc();

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath+"out/"+ rep+"/", "COMM_PLUS_WM_1M_"+i+".shp");
		}

		LOGGER.info("End");
	}


	public static TesselationGeneralisationSpecifications specs = new TesselationGeneralisationSpecifications() {
		public void setTesselationConstraints(ATesselation t, CartographicResolution res) {
			//t.addConstraint(new CTesselationMorphology(t, res.getSeparationDistanceMeter(), 1e-5, 5));
		}
		public void setUnitConstraints(ATesselation t, CartographicResolution res) {
			/*for(AUnit a : t.aUnits) {
				//a.addConstraint(new CUnitNoNarrowGaps(a, resolution, 0.1*resSqu, 4).setPriority(10));
				//a.addConstraint(new ConstraintOneShot<AUnit>(a, new TUnitNarrowGapsFilling(a, resolution, 0.1*resSqu, 4)).setPriority(10));
			}*/
		}
		public void setTopologicalConstraints(ATesselation t, CartographicResolution res) {
			for(AFace a : t.aFaces) {
				a.addConstraint(new CFaceSize(a, 0.2*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), true, true).setPriority(2));
				a.addConstraint(new CFaceValidity(a).setPriority(1));
				a.addConstraint(new CFaceEEZInLand(a).setPriority(10));
				//a.addConstraint(new CFaceNoSmallHoles(a, resSqu*5).setPriority(3));
				//a.addConstraint(new CFaceNoEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()).setPriority(1));
			}
			for(AEdge a : t.aEdges) {
				a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM(), true));
				a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
				a.addConstraint(new CEdgeValidity(a));
				a.addConstraint(new CEdgeTriangle(a));
				//a.addConstraint(new CEdgeSize(a, resolution, resolution*0.6));
				//a.addConstraint(new CEdgeNoSelfIntersection(a));
				//a.addConstraint(new CEdgeToEdgeIntersection(a, graph.getSpatialIndexEdge()));
			}
		}
	};

}
