/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.algo.graph.GraphBuilder;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;
import org.opengis.filter.Filter;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayQuality {
	public final static Logger LOGGER = Logger.getLogger(MainRailwayQuality.class.getName());

	public static void main(String[] args) throws Exception {

		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";
		//String inFile = basePath+"out/quality/railway.shp";
		Filter fil = null; //CQL.toFilter( "CNTR = 'ES'" );
		Collection<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;

		//check identifier
		//HashMap<String, Integer> out = FeatureUtil.checkIdentfier(secs, "id");
		//System.out.println(out);

		for(Feature f : secs) f.id = f.get("id").toString();
		LOGGER.info(secs.size()+" sections - " + FeatureUtil.getVerticesNumber(secs)+" vertices.");

		//
		secs = GraphBuilder.qualityFixForSections(secs);

		//LOGGER.info("Check section intersection");
		//GraphBuilder.checkSectionsIntersection(secs);

		//g = GraphBuilder.buildFromLinearFeaturesPlanar(secs, true);
		//System.out.println("ok!!!");

		LOGGER.info("Save - nb=" + secs.size());
		SHPUtil.saveSHP(secs, basePath+"out/quality/railway.shp", SHPUtil.getCRS(inFile));

		LOGGER.info("End");
	}

}
