/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.algo.graph.GraphConnexComponents;
import org.opencarto.algo.measure.Circularity;
import org.opencarto.algo.measure.Elongation;
import org.opencarto.algo.measure.Elongation.WidthApproximation;
import org.opencarto.datamodel.Feature;
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
public class MainORMGene {
	public final static Logger LOGGER = Logger.getLogger(MainORMGene.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		//focus on sweden. Focus first on tracks
		//https://www.openrailwaymap.org/
		//https://wiki.openstreetmap.org/wiki/OpenRailwayMap/Tagging

		//is it possible to filter OSM based on tagging to obtain something comparable to ERM? -> Yes.
		//filtering: ambiguity with "abandonned". Choice: remove it. Tramway/subway. Choice: remove it and focus on train.
		/*
"railway"!='construction'
AND "railway"!='dismantled'
AND "railway"!='elevator'
AND "railway"!='funicular'
AND "railway"!='historic'
AND "railway"!='historic_path'
AND "railway"!='historical'
AND "railway" != 'miniature'
AND "railway"!='planned'
AND "railway" != 'platform'
AND "railway" != 'platform_edge'
AND "railway" != 'proposed'
AND "railway" != 'razed'
AND "railway" != 'turntable'
AND "railway" != 'abandoned'
AND "railway" != 'tram'
AND "railway" != 'subway'
		 */
		//Conclusion: filtering analysis based on tags. Accept imperfection! Analyse it. Make choices and approximations. OSM has a descriptive approach, while we have functionnal questions.

		//Is it possible to use some tags to select tracks composing main lines ? Approach: examine other tags and make classification
		//service? usage? maxspeed-highspeed? electrified-voltage? gauge? railway:traffic_mode? railway:preferred_direction-railway:bidirectional? name-description-ref?
		//use service! - keep only tracks with service==null (+ voltage!=null ?)

		//target: 1:50k -> Resolution 0.2mm -> 10m

		//TODO define specs based on ORM model and generalisation process in mind (at least ERM specs should be covered)
		//specs for input dataset (1:5k): tracks selected, with proper attributes, well structured. basic ETL process.
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces)
		//   leveling crossing (points)

		//make generalisation on graph
		//collapse too short faces/edges
		//algorithm to compute average of two lines, based on curvelinear abscissa


		LOGGER.info("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		int epsg = 3035;
		//ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/orm_tracks.shp", epsg).fs;
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/lines_LAEA.shp", epsg).fs;
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

		LOGGER.info("Get Faces");
		Collection<Feature> faces = g.getFaceFeatures(epsg); g = null;
		for(Feature f : faces) {
			WidthApproximation wa = Elongation.getWidthApproximation((Polygon) f.getGeom());
			f.getProperties().put("e_width", wa.width);
			f.getProperties().put("e_length", wa.length);
			f.getProperties().put("e_elong", wa.elongation);
			f.getProperties().put("circularity", Circularity.get(f.getGeom()));
		}




		LOGGER.info("Save faces+");
		SHPUtil.saveSHP(faces, basePath+"out/", "facesPlus.shp");

		//TODO label edges with 'line obstacle' flag (if it is short, separating 2 long elements () or small + inflexion point?)
		//TODO build 'chains of narrow stuff'

		//TODO collapse small faces + small edges (<10m)
		//TODO deal with circular faces
		//TODO detect nodes whose deletion would destroy connection (name?)
		//TODO compute nodes centrality?
		//TODO narrow face collapse algorithm - with triangulation? - collapse cases when only 2 limit sections?
		//TODO generate areas of service lines

		System.out.println("End");
	}

	/*
ERM specifications - railways

--- Railway station

type - railway station, joint railway station, halt, marshalling yard, intermodal rail transport, terminal
name
railway_station_identifier
use - cargo/freight, passenger, general

--- Railway section

name
railway_code
existence - operational, abandoned/disused, under construction
category - main line, branch line
use - cargo/freight, passenger, general
nb_tracks - single, double, multiple, juxtaposition
gauge_cm
gauge - normal, braod, narrow
power_source - electrified track, overhead electrified, non-electrified
speed_class - conventionnal, upgraded high speed (<250), dedicated high speed (>250)
level - -9,-3,-2,-1,0,1,2,3,9
seasonality - all year, seasonal
TEN - part of, not part of
length_m

--- Railway network link
No attribute

	 */

}
