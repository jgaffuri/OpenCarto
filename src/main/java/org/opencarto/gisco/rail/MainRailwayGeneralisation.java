/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.opencarto.algo.graph.GraphBuilder;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;
import org.opengis.filter.Filter;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayGeneralisation {
	public final static Logger LOGGER = Logger.getLogger(MainRailwayGeneralisation.class.getName());

	public static void main(String[] args) throws Exception {

		//target: 1:50k -> Resolution 0.2mm -> 10m
		double resolution = 10;
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces) + leveling crossing (points) ? All infos from RINF, etc.


		//test
		//lines = Resolutionise.applyLinear(lines, res);
		//GraphSimplify.resPlanifyLines(Collection<Geometry> lines, double res) {



		//TODO
		//complete graph lib reorganisation/debugging

		//data enrichment
		//build strokes - give imprtance to sections depending on criteria: stroke, attributes, connectivity. Proximity of stations. dead-end

		//detect conflicts/algos:
		//- too narrow/small&compact domains - collapse
		//- too short sections
		//- deal with circular faces
		//- generate areas of service / station areas, with faces around deleted sections

		//explore:
		//mst of nodes - make clusters
		//detect nodes whose deletion would destroy connection (name?)
		//compute nodes centrality?
		//label edges with 'line obstacle' flag (if it is short, separating 2 long elements () or short + inflexion point?)


		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";
		Filter fil = null; //CQL.toFilter( "CNTR = 'NL'" );
		ArrayList<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;
		LOGGER.info(secs.size()+"   "+FeatureUtil.getVerticesNumber(secs));


		LOGGER.info("Build graph"); // non planar
		Graph g = GraphBuilder.buildFromLinearFeaturesNonPlanar(secs);

		LOGGER.info("collapse too short edges");
		//TODO buggy...
		//g.collapseTooShortEdges(resolution);

		//TODO extract graph algorithms from graph datamodel
		//get pairs of edges


		/*/get partition
		Collection<Feature> parts = Partition.getPartitionDataset(secs, 50000, 100000000, Partition.GeomType.ONLY_LINES, 0);
		SHPUtil.saveSHP(parts, basePath+"out/partition.shp", SHPUtil.getCRS(inFile));
		 */

		/*/make service areas, with buffering
		RailwayServiceAreasBufferDetection rsad = new RailwayServiceAreasBufferDetection(secs);
		rsad.compute(50000, 100000000);
		SHPUtil.saveGeomsSHP(rsad.getServiceAreas(), basePath+"out/service_areas.shp", SHPUtil.getCRS(inFile));
		SHPUtil.saveGeomsSHP(rsad.getDoubleTrackAreas(), basePath+"out/double_tracks_areas.shp", SHPUtil.getCRS(inFile));
		 */






		/*
		LOGGER.info("Build graph");
		Graph g = GraphBuilder.buildForNetwork(FeatureUtil.getGeometriesMLS(secs));

		//LOGGER.info("Compute GCC");
		//Collection<Graph> gs = GraphConnexComponents.get(g);
		//406 connex components.
		//all less than 20 nodes, except - 24,24,1858,39,26
		//TODO need for interactive validation for connectivity correction. Add fictive links to reconnect connex components.
		LOGGER.info("Get main component");
		g = GraphConnexComponents.getMainNodeNb(g);

		//TODO flag potential connectivity issues
		//For each end node pair, compute ratio of graph distance over euclidian distance.

		LOGGER.info("Rebuild graph");
		g = GraphBuilder.buildFromEdges(g.getEdges());

		//LOGGER.info("Save graph");
		//GraphSHPUtil.exportAsSHP(g, basePath+"out/", ProjectionUtil.getETRS89_LAEA_CRS());

		LOGGER.info("Get edges and faces");
		Collection<Feature> faces = g.getFaceFeatures();
		Collection<Feature> edges = g.getEdgeFeatures();

		LOGGER.info("Analyse edges");
		for(Feature f : edges) {
			Edge e = g.getEdge(f.id);
		}

		LOGGER.info("Analyse faces");
		for(Feature f : faces) {
			WidthApproximation wa = Elongation.getWidthApproximation((Polygon) f.getGeom());
			f.set("e_width", wa.width);
			f.set("e_length", wa.length);
			f.set("e_elong", wa.elongation);
			f.set("circ", Circularity.get(f.getGeom()));
		}
		g = null;
		 */


		//LOGGER.info("Build strokes");
		//Collection<Stroke> sts = new StrokeAnalysis(secs, false).run(0.6).getStrokes();

		/*/TODO define and use importance criteria. Use it in salience definition (for both connections and strokes (representative))
		Comparator<Feature> comp = new Comparator<Feature>() {
			@Override
			public int compare(Feature f1, Feature f2) {
				return 0;
			}
		};*/


		System.out.println("End");
	}

}
