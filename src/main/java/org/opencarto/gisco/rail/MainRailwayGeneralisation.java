/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
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
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces) + leveling crossing (points) ? All infos from RINF, etc.

		// selection first, based on graph analysis. connectivity + stroke + faces?
		//TODO collapse too short edges (<res) / too small (and compact) faces (<res*res).

		//TODO narrow face collapse algorithm - use ls pair average algorithm

		//TODO deal with circular faces
		//TODO detect nodes whose deletion would destroy connection (name?)
		//TODO compute nodes centrality?

		//TODO generate areas of service lines, with faces
		//TODO label edges with 'line obstacle' flag (if it is short, separating 2 long elements () or short + inflexion point?)
		//TODO build 'chains of narrow stuff'



		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";
		Filter fil = null; //CQL.toFilter( "CNTR = 'NL'" );
		ArrayList<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;
		LOGGER.info(secs.size()+"   "+FeatureUtil.getVerticesNumber(secs));


		//get partition
		Collection<Feature> parts = Partition.getPartitionDataset(secs, 50000, 100000000, Partition.GeomType.ONLY_LINES, 0);
		//SHPUtil.saveSHP(parts, basePath+"out/partition.shp", SHPUtil.getCRS(inFile));

		
		//make service areas, with buffering
		RailwayServiceAreasDetection rsad = new RailwayServiceAreasDetection(secs);
		rsad.compute(50000, 100000000);
		SHPUtil.saveGeomsSHP(rsad.getServiceAreas(), basePath+"out/service_areas.shp", SHPUtil.getCRS(inFile));
		SHPUtil.saveGeomsSHP(rsad.getDoubleTrackAreas(), basePath+"out/double_tracks_areas.shp", SHPUtil.getCRS(inFile));






		/*
		LOGGER.info("Compute graph");
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
