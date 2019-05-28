/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opencarto.algo.graph.GraphBuilder;
import org.opencarto.algo.graph.GraphToFeature;
import org.opencarto.algo.graph.NodeReduction;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
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
		Filter fil = CQL.toFilter( "CNTR = 'ES'" );
		Collection<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;
		for(Feature f : secs) f.id = f.get("id").toString();
		LOGGER.info(secs.size()+"   " + FeatureUtil.getVerticesNumber(secs));

		GraphBuilder.checkIntersectionSections(secs);

		LOGGER.info("Fix");
		secs = GraphBuilder.fixIntersectionSections(secs);
		LOGGER.info(secs.size());

		LOGGER.info("New check");
		GraphBuilder.checkIntersectionSections(secs);

		LOGGER.info("Decompose");
		secs = FeatureUtil.getFeaturesWithSimpleGeometrie(secs);
		LOGGER.info(secs.size());

		LOGGER.info("New check");
		GraphBuilder.checkIntersectionSections(secs);

		LOGGER.info("Ensure node reduction");
		Graph g = GraphBuilder.buildFromLinearFeaturesNonPlanar(secs);
		Collection<Edge> nres = NodeReduction.ensure(g);
		secs = GraphToFeature.getAttachedFeatures(g.getEdges());




		LOGGER.info("End");
	}



}
