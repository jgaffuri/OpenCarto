/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.geotools.filter.text.cql2.CQL;
import org.opencarto.algo.graph.stroke.Stroke;
import org.opencarto.algo.graph.stroke.StrokeAnalysis;
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
		//specs for generalised dataset (1:50k)
		//   main railway lines + railway areas + stations (points and surfaces)
		//   leveling crossing (points)

		
		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";
		Filter fil = null; //CQL.toFilter( "CNTR = 'NL'" );
		ArrayList<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;
		LOGGER.info(secs.size()+"   "+FeatureUtil.getVerticesNumber(secs));


		//get partition
		//Collection<Feature> parts = Partition.getPartitionDataset(secs, 50000, 100000000, Partition.GeomType.ONLY_LINES);
		//SHPUtil.saveSHP(parts, basePath+"out/partition.shp", SHPUtil.getCRS(inFile));

		RailwayServiceAreasDetection rsad = new RailwayServiceAreasDetection(secs);
		rsad.compute(50000, 100000000);

		LOGGER.info("Save");
		SHPUtil.saveGeomsSHP(rsad.getServiceAreas(), basePath+"out/service_areas.shp", SHPUtil.getCRS(inFile));
		SHPUtil.saveGeomsSHP(rsad.getDoubleTrackAreas(), basePath+"out/double_tracks_areas.shp", SHPUtil.getCRS(inFile));

		
		
		//TODO design overall generalisation algorithm based on:
		// selection first, based on connectivity + stroke + faces?
		// collapse, based on narrow faces

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
