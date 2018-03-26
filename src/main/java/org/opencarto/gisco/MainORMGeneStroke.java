/**
 * 
 */
package org.opencarto.gisco;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

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

		LOGGER.info("Build network graph");
		for(Feature t : tracks) {
			MultiLineString mls = (MultiLineString)t.getGeom();
			if(mls.getNumGeometries() != 1) LOGGER.warn("Unexpected number of lines in geometry. Schould be one.");
			t.setGeom((LineString)mls.getGeometryN(0));
		}
		Graph g = GraphBuilder.buildForNetworkFromLinearFeatures(tracks);

		//LOGGER.info("Build strokes");
		//for each node, attach list of section pairs, which are "aligned" (angle of deflection)
		//go through list of pairs and build strokes as list of sections
		//define salience of stroke (based on length and attributes)

		System.out.println("End");
	}

}
