/**
 * 
 */
package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.AEdge;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeGranularity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgeValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.CEdgesFacesContainPoints;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceContainPoints;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceNoTriangle;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceSize;
import org.opencarto.transfoengine.tesselationGeneralisation.CFaceValidity;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisationSpecification;
import org.opencarto.util.ProjectionUtil.CRSType;

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class MainGenNUTSPlusXM {
	private final static Logger LOGGER = Logger.getLogger(MainGenNUTSPlusXM.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		//TesselationGeneralisation.LOGGER.setLevel(Level.OFF);
		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		LOGGER.info("Load pts data");
		final HashMap<String, Collection<Point>> ptsData = loadPoints(basePath);

		//for(double s : new double[]{3,10,20,60}) {
		//for(double s : new double[]{60,20,10,3}) {
		for(double s : new double[]{10,3}) {
			double scaleDenominator = s*1e6;

			//define specifications
			TesselationGeneralisationSpecification specs = new TesselationGeneralisationSpecification(scaleDenominator, CRSType.CARTO) {
				public void setTopologicalConstraints(ATesselation t) {
					for(AFace a : t.aFaces) {
						a.addConstraint(new CFaceSize(a, 0.1*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), preserveAllUnits, preserveIfPointsInIt).setPriority(2));
						a.addConstraint(new CFaceValidity(a));
						if(preserveIfPointsInIt) a.addConstraint(new CFaceContainPoints(a));
						if(noTriangle) a.addConstraint(new CFaceNoTriangle(a));
						//difference here
						a.addConstraint(new CFaceEEZInLand(a).setPriority(10));
					}
					for(AEdge a : t.aEdges) {
						a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM()));
						a.addConstraint(new CEdgeValidity(a));
						if(noTriangle) a.addConstraint(new CEdgeNoTriangle(a));
						a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
						if(preserveIfPointsInIt) a.addConstraint(new CEdgesFacesContainPoints(a));
					}
				}
			};

			LOGGER.info("Load data for "+((int)s)+"M generalisation");
			String inFile = basePath+"nutsplus/NUTS_PLUS_01M_1403_WM.shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile).fs;
			for(Feature f : units) for(String id : new String[] {"NUTS_P_ID","NUTS_CODE","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

			LOGGER.info("Launch generalisation for "+((int)s)+"M");
			int roundNb = 8;
			LOGGER.error("FIX !!!");
			units = TesselationGeneralisation.runGeneralisation(units, ptsData, specs, roundNb, 1000000, 1000);
			if(true) return;

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath + "out/nutsplus/NUTS_PLUS_"+((int)s)+"M_WM.shp");
		}
		LOGGER.info("End");
	}


	private static HashMap<String,Collection<Point>> loadPoints(String basePath) {
		HashMap<String,Collection<Point>> index = new HashMap<String,Collection<Point>>();
		for(String file : new String[] {"cntr_pts","nuts_p_pts"})
			for(Feature f : SHPUtil.loadSHP(basePath+"nutsplus/pts/"+file+".shp").fs) {
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
