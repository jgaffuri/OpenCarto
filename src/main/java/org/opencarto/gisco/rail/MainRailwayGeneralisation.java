/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.PartitionedOperation;
import org.opencarto.util.FeatureUtil;
import org.opengis.filter.Filter;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayGeneralisation {
	public final static Logger LOGGER = Logger.getLogger(MainRailwayGeneralisation.class.getName());

	public static void main(String[] args) throws Exception {

		LOGGER.info("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		String inFile = basePath+"out/EM/RailwayLinkEM.shp";
		Filter fil = null; //CQL.toFilter( "CNTR = 'NL'" );
		ArrayList<Feature> secs = SHPUtil.loadSHP(inFile, fil).fs;
		LOGGER.info(secs.size()+"   "+FeatureUtil.getVerticesNumber(secs));


		//get partition
		//Collection<Feature> parts = Partition.getPartitionDataset(secs, 100000, 100000000, Partition.GeomType.ONLY_LINES);
		//SHPUtil.saveSHP(parts, basePath+"out/partition.shp", SHPUtil.getCRS(inFile));

		ArrayList<Geometry> sas = new ArrayList<Geometry>();
		ArrayList<Geometry> dts = new ArrayList<Geometry>();
		Partition.runRecursively(secs, new PartitionedOperation() {
			@Override
			public void run(Partition p) {
				LOGGER.info(p.getCode());

				RailwayServiceAreasDetection rsad = new RailwayServiceAreasDetection(p.getFeatures());
				rsad.compute();

				sas.addAll(rsad.getServiceAreas());
				dts.addAll(rsad.getDoubleTrackAreas());

			}}, 100000, 100000000, false, Partition.GeomType.ONLY_LINES);

		LOGGER.info("Save");
		SHPUtil.saveGeomsSHP(sas, basePath+"out/service_areas.shp", SHPUtil.getCRS(inFile));
		SHPUtil.saveGeomsSHP(dts, basePath+"out/double_tracks_areas.shp", SHPUtil.getCRS(inFile));

		System.out.println("End");
	}

}
