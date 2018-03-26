/**
 * 
 */
package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.algo.graph.GraphConnexComponents;
import org.opencarto.algo.graph.StrokeAnalysis;
import org.opencarto.algo.graph.StrokeAnalysis.Stroke;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.GraphSHPUtil;
import org.opencarto.io.SHPUtil;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * 
 * Some tests on ORM generalisation
 * 
 * @author julien Gaffuri
 *
 */
public class MainORMGeneStroke {
	public final static Logger LOGGER = Logger.getLogger(MainORMGeneStroke.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		LOGGER.info("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		int epsg = 3035;
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/orm_tracks.shp", epsg).fs;

		LOGGER.info("Build network graph (non planar)");
		for(Feature t : tracks) {
			MultiLineString mls = (MultiLineString)t.getGeom();
			if(mls.getNumGeometries() != 1) LOGGER.warn("Unexpected number of lines in geometry. Schould be one.");
			t.setGeom((LineString)mls.getGeometryN(0));
		}
		Graph g = GraphBuilder.buildForNetworkFromLinearFeatures(tracks);

		LOGGER.info("Get main component");
		g = GraphConnexComponents.getMainNodeNb(g);
		//System.out.println(g.getNodes().size()); //8621 nodes

		LOGGER.info("Save graph");
		GraphSHPUtil.exportAsSHP(g, "/home/juju/Bureau/gisco_rail/out/non_planar/", 3035);

		LOGGER.info("Build strokes");
		Collection<Stroke> sts = new StrokeAnalysis(g).getStrokes();
		//define salience of stroke (based on length and attributes)

		//export strokes

		System.out.println("End");
	}

}