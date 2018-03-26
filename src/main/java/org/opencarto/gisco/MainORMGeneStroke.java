/**
 * 
 */
package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.algo.graph.GraphConnexComponents;
import org.opencarto.algo.measure.Circularity;
import org.opencarto.algo.measure.Elongation;
import org.opencarto.algo.measure.Elongation.WidthApproximation;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.GraphSHPUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

import com.vividsolutions.jts.geom.Polygon;

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

		//https://www.openrailwaymap.org/
		//https://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging

		//target: 1:50k -> Resolution 0.2mm -> 10m
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces)
		//   leveling crossing (points)

		LOGGER.info("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		int epsg = 3035;
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/orm_tracks.shp", epsg).fs;
		//ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/lines_LAEA.shp", epsg).fs;
		//System.out.println(tracks.size()+"   "+FeatureUtil.getVerticesNumber(tracks));

		LOGGER.info("Compute graph");
		Graph g = GraphBuilder.buildForNetwork(FeatureUtil.getGeometriesMLS(tracks));

		LOGGER.info("Compute GCC");
		Collection<Graph> gs = GraphConnexComponents.get(g);
		//406 connex components.
		//all less than 20 nodes, except - 24,24,1858,39,26
		for(Graph g_ : gs) {
			if(g_.getNodes().size() >= 1858) {
				g = g_;
				break;
			}
		}

		//TODO need for interactive validation for connectivity correction. Add fictive links to reconnect connex components.
		//For each end node pair, compute ratio of graph distance over euclidian distance.

		LOGGER.info("Rebuild graph");
		g = GraphBuilder.buildFromEdges(g.getEdges());

		//LOGGER.info("Save graph");
		GraphSHPUtil.exportAsSHP(g, basePath+"out/", epsg);

		LOGGER.info("Get edges and faces");
		Collection<Feature> faces = g.getFaceFeatures(epsg);
		Collection<Feature> edges = g.getEdgeFeatures(epsg);

		LOGGER.info("Analyse edges");
		for(Feature f : edges) {
			Edge e = g.getEdge(f.id);
		}

		LOGGER.info("Analyse faces");
		for(Feature f : faces) {
			WidthApproximation wa = Elongation.getWidthApproximation((Polygon) f.getGeom());
			f.getProperties().put("e_width", wa.width);
			f.getProperties().put("e_length", wa.length);
			f.getProperties().put("e_elong", wa.elongation);
			f.getProperties().put("circ", Circularity.get(f.getGeom()));
		}
		g = null;

		LOGGER.info("Save edges+");
		SHPUtil.saveSHP(faces, basePath+"out/", "edgesPlus.shp");
		LOGGER.info("Save faces+");
		SHPUtil.saveSHP(faces, basePath+"out/", "facesPlus.shp");

		//TODO build strokes:
		//for each node, attach list of section pairs, which are "aligned" (angle of deflection)
		//go through list of pairs and build strokes as list of sections
		//define salience of stroke (based on length and attributes)

		//TODO label edges with 'line obstacle' flag (if it is short, separating 2 long elements () or small + inflexion point?)
		//TODO build 'chains of narrow stuff'

		//TODO collapse small faces + small edges (<10m)
		//TODO deal with circular faces
		//TODO detect nodes whose deletion would destroy connection (name?)
		//TODO compute nodes centrality?
		//TODO narrow face collapse algorithm - with triangulation? - collapse cases when only 2 limit sections?
		//TODO generate areas of service lines
		//collapse too short faces/edges
		//algorithm to compute average of two lines, based on curvelinear abscissa
		//TODO connection with RINF-ERA - UIC RailTopoModel
		//TODO produce network for statistical reporting

		System.out.println("End");
	}

}
