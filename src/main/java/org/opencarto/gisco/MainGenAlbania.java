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
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class MainGenAlbania {
	private final static Logger LOGGER = Logger.getLogger(MainGenAlbania.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		String basePath = "/home/juju/Bureau/alb/";

		for(double s : new double[]{1,3,10,20,60}) {
			double scaleDenominator = s*1e6;

			LOGGER.info("Load data for "+((int)s)+"M generalisation");
			final int epsg = 3857; String inFile = basePath+"/SU_AL_100k.shp";
			//final int epsg = 3857; String inFile = basePath+"/out/nutsplus/NUTS_PLUS_10M_WM_6.shp";
			Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
			for(Feature f : units) f.id = ""+f.getProperties().get("LVL2");

			LOGGER.info("Launch generalisation for "+((int)s)+"M");
			int roundNb = 10;
			units = TesselationGeneralisation.runGeneralisation(units, null, null, scaleDenominator, roundNb, false, 1000000, 1000);

			LOGGER.info("Save output data");
			SHPUtil.saveSHP(units, basePath + "/SU_AL_"+((int)s)+"M.shp");
		}
		LOGGER.info("End");
	}


	private static HashMap<String,Collection<Point>> loadPoints(String basePath) {
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
