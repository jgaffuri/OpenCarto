/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgesFacesContainPoints;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceContainPoints;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CTesselationMorphology;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisationSpecifications;

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOGeneXM {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeneXM.class.getName());


	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		LOGGER.info("Load pts data");
		final HashMap<String, Collection<Point>> ptsIndex = getPoints(basePath);

		//define specifications
		TesselationGeneralisationSpecifications specs = new TesselationGeneralisationSpecifications() {
			public void setTesselationConstraints(ATesselation t, CartographicResolution res) {
				t.addConstraint(new CTesselationMorphology(t, res.getSeparationDistanceMeter(), 1e-5));
			}
			public void setUnitConstraints(ATesselation t, CartographicResolution res) {}
			public void setTopologicalConstraints(ATesselation t, CartographicResolution res) {
				for(AFace a : t.aFaces) {
					//get points in face
					Collection<Point> pts = CFaceContainPoints.getPointsInFace(a,ptsIndex);
					a.addConstraint(new CFaceSize(a, 0.2*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), true, pts ).setPriority(2));
					a.addConstraint(new CFaceValidity(a));
					a.addConstraint(new CFaceEEZInLand(a).setPriority(10));
					if(pts != null)
						a.addConstraint(new CFaceContainPoints(a, pts));
				}
				for(AEdge a : t.aEdges) {
					a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM(), true));
					a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
					a.addConstraint(new CEdgeValidity(a));
					a.addConstraint(new CEdgeTriangle(a));
					Collection<Point> pts1 = CFaceContainPoints.getPointsInFace(a.getAtesselation().getAFace(a.getObject().f1), ptsIndex);
					Collection<Point> pts2 = CFaceContainPoints.getPointsInFace(a.getAtesselation().getAFace(a.getObject().f2), ptsIndex);
					if(pts1 != null && pts2 != null)
						a.addConstraint(new CEdgesFacesContainPoints(a,pts1,pts2));
				}
			}
		};

		for(double s : new double[]{3,10,20,60}) {
			double scaleDenominator = s*1e6;

			LOGGER.info("Load data for "+((int)s)+"M");
			final int epsg = 3857; String inFile = basePath+"nutsplus/NUTS_PLUS_01M_1403_WM.shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
			for(Feature f : units) for(String id : new String[] {"NUTS_P_ID","NUTS_CODE","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

			//launch several rounds
			for(int i=1; i<=8; i++) {
				LOGGER.info("Launch generalisation " + i + " for "+((int)s)+"M");
				units = DefaultTesselationGeneralisation.runGeneralisation(units, specs, scaleDenominator, 1, false);

				LOGGER.info("Run GC");
				System.gc();

				LOGGER.info("Save output data");
				SHPUtil.saveSHP(units, basePath + "out/nutsplus/", "NUTS_PLUS_"+((int)s)+"M_WM_"+i+".shp");
			}

		}

		LOGGER.info("End");
	}


	private static HashMap<String,Collection<Point>> getPoints(String basePath) {
		HashMap<String,Collection<Point>> index = new HashMap<String,Collection<Point>>();
		for(String file : new String[] {"cntr_pts","nuts_p_pts"})
			for(Feature f : SHPUtil.loadSHP(basePath+"nutsplus/pts/"+file+".shp", 3857).fs) {
				String id = (String)f.getProperties().get("CNTR_ID");
				if(id == null) id = (String)f.getProperties().get("NUTS_P_ID");
				if("".equals(id)) continue;
				Collection<Point> data = index.get(id);
				if(data == null) { data=new ArrayList<Point>(); index.put(id, data); }
				data.add((Point) f.getGeom());
			}
		return index;
	}

}
