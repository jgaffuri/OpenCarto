/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
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

		//TODO
		//complete graph lib reorganisation/debugging

		//data enrichment
		//build strokes - give imprtance to sections depending on criteria: stroke, attributes, connectivity. Proximity of stations. dead-end

		//detect conflicts/algos:
		//- too narrow/small&compact domains - collapse
		//- too short sections
		//- deal with circular faces
		//- generate areas of service /getFeaturesWithSimpleGeometrie station areas, with faces around deleted sections

		//explore:
		//mst of nodes - make clusters
		//detect nodes whose deletion would destroy connection (name?)
		//compute nodes centrality?
		//label edges with 'line obstacle' flag (if it is short, separating 2 long elements () or short + inflexion point?)


		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";
		Filter fil = null; //CQL.toFilter( "CNTR = 'NL'" );
		Collection<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;
		for(Feature f : secs) f.id = f.get("id").toString();
		LOGGER.info(secs.size()+" sections - " + FeatureUtil.getVerticesNumber(secs)+" vertices.");


		//g = GraphBuilder.buildFromLinearFeaturesPlanar(secs, true);


		//tests on resolutionise

		/*
		for(int scalek : new int[] {10, 50, 100, 250, 500, 1000}) {
			LOGGER.info("Resolutionise " + scalek);
			Collection<LineString> lss = JTSGeomUtil.getLineStrings( FeatureUtil.featuresToGeometries(secs) );
			Collection<LineString> out = GraphSimplify.resPlanifyLines(lss, Util.getGroundResolution(scalek), true);

			LOGGER.info("Save");
			SHPUtil.saveGeomsSHP(out, basePath+"out/resolutionised/resolutionised_"+scalek+"k.shp", SHPUtil.getCRS(inFile));
		}
		 */


		/*
		LOGGER.info("Ensure node reduction");
		Graph g = GraphBuilder.buildFromLinearFeaturesNonPlanar(secs);
		Collection<Edge> nres = NodeReduction.ensure(g);

		LOGGER.info(nres.size() + " edges deleted after node reduction");
		LOGGER.info(GraphToFeature.getAttachedFeatures(g.getEdges()).size() + " features remaining");
		secs = GraphToFeature.getAttachedFeatures(g.getEdges());
		//NodingUtil.fixLineStringsIntersectionNoding(secs);

		//Collection<LineString> geoms = JTSGeomUtil.getLineStrings( FeatureUtil.getGeometries(secs) );
		//Graph g = GraphBuilder.buildFromLinearGeometriesPlanar(geoms, true);
		g = GraphBuilder.buildFromLinearFeaturesPlanar(secs, true);
		 */

		//edge collapse
		/*
		LOGGER.info("Build graph"); // non planar
		//Graph g = GraphBuilder.buildFromLinearFeaturesPlanar(secs, false); //TODO debug that !
		Graph g = GraphBuilder.buildFromLinearFeaturesNonPlanar(secs);

		LOGGER.info("Ensure node reduction");
		Collection<Edge> nres = NodeReduction.ensure(g);
		LOGGER.info(nres.size() + " edges deleted after node reduction");
		LOGGER.info(GraphToFeature.getAttachedFeatures(g.getEdges()).size() + " features remaining");

		LOGGER.info("collapse too short edges");
		Collection<LineString> collapsed_edges = EdgeCollapse.collapseTooShortEdges(g, resolution, true);
		LOGGER.info("Collapsed edges: " + collapsed_edges.size());

		LOGGER.info("Save collapsed edges");
		SHPUtil.saveGeomsSHP(collapsed_edges, basePath+"out/edge_collapse/collapsed_edges.shp", SHPUtil.getCRS(inFile));

		//edge pairs collapse
		secs = GraphToFeature.getAttachedFeatures(g.getEdges());
		NodingUtil.fixLineStringsIntersectionNoding(secs);
		g = GraphBuilder.buildFromLinearFeaturesPlanar(secs, true);

		//build graph with faces
		//get narrow faces
		//get faces with only two sections
		//collapse face

		LOGGER.info("Final edges: " + g.getEdges().size());
		GraphToFeature.updateEdgeLinearFeatureGeometry(g.getEdges());
		secs = GraphToFeature.getAttachedFeatures(g.getEdges());
		LOGGER.info("Final sections: " + secs.size());
		SHPUtil.saveSHP(secs, basePath+"out/edge_collapse/sections_after_collapse.shp", SHPUtil.getCRS(inFile));
		 */







		/*/get partition
		Collection<Feature> parts = Partition.getPartitionDataset(secs, 50000, 100000000, Partition.GeomType.ONLY_LINES, 0);
		SHPUtil.saveSHP(parts, basePath+"out/partition/partition.shp", SHPUtil.getCRS(inFile));
		 */



		/*/make service areas, with buffering
		RailwayServiceAreasBufferDetection rsad = new RailwayServiceAreasBufferDetection(secs);
		rsad.compute(50000, 100000000);
		SHPUtil.saveGeomsSHP(rsad.getServiceAreas(), basePath+"out/service_area/service_areas.shp", SHPUtil.getCRS(inFile));
		SHPUtil.saveGeomsSHP(rsad.getDoubleTrackAreas(), basePath+"out/service_area/double_tracks_areas.shp", SHPUtil.getCRS(inFile));
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


		LOGGER.info("End");
	}

}
