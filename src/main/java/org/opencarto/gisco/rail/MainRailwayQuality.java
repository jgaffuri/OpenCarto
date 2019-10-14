/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opengis.filter.Filter;

import eu.europa.ec.eurostat.eurogeostat.algo.graph.GraphBuilder;
import eu.europa.ec.eurostat.eurogeostat.datamodel.Feature;
import eu.europa.ec.eurostat.eurogeostat.io.SHPUtil;
import eu.europa.ec.eurostat.eurogeostat.util.FeatureUtil;

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

		for(Feature f : secs) f.setID( f.getAttribute("id").toString() );
		LOGGER.info(secs.size()+" sections - " + FeatureUtil.getVerticesNumber(secs)+" vertices.");

		LOGGER.info("Quality fix");
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
