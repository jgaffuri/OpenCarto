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
import org.opencarto.util.ProjectionUtil;

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
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/orm_tracks.shp").fs;

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
		GraphSHPUtil.exportAsSHP(g, basePath+"out/non_planar/", ProjectionUtil.getETRS89_LAEA_CRS());

		LOGGER.info("Build strokes");
		Collection<Stroke> sts = new StrokeAnalysis(g).run(45).getStrokes();

		LOGGER.info("Export strokes");
		SHPUtil.saveSHP(sts, basePath+"out/non_planar/strokes.shp", ProjectionUtil.getETRS89_LAEA_CRS());

		System.out.println("End");
	}

}
