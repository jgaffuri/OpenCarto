package org.opencarto.processes;

import org.locationtech.jts.index.quadtree.Quadtree;

import eu.europa.ec.eurostat.eurogeostat.algo.clustering.AggregationWithSpatialIndex;
import eu.europa.ec.eurostat.eurogeostat.algo.clustering.Clustering;
import eu.europa.ec.eurostat.eurogeostat.algo.clustering.FeatureClusteringIndex;
import eu.europa.ec.eurostat.eurogeostat.algo.distances.FeatureDistance;
import eu.europa.ec.eurostat.eurogeostat.datamodel.Feature;
import eu.europa.ec.eurostat.eurogeostat.io.SHPUtil;
import eu.europa.ec.eurostat.eurogeostat.io.SHPUtil.SHPData;

public class SHPGeneralisationProcess {

	/**
	 * Do clustering generalisation for point shp file (ideally proportional circles)
	 * 
	 * @param inPath input shp file
	 * @param file input shp file
	 * @param outPath output shp file
	 * @param resolutions the resolutions, from lowest to largest
	 * @param factor factor to force more (>1) or less (>1) generalisation
	 * @param projected if the geom are projected (in m) or not (in degree)
	 * @param agg the aggregation method
	 * @param index the spatial index to use in the clustering (can be null)
	 */
	public static void perform(String inPath, String file, String outPath, double[] resolutions,
			double factor, boolean skipFirst, boolean projected, AggregationWithSpatialIndex<Feature> agg){
		System.out.println("Load data from "+inPath+file);
		SHPData data = SHPUtil.loadSHP(inPath + file + ".shp");

		int nb = resolutions.length;
		for(int i=0; i<nb; i++){
			System.out.println("Clustering (level "+(nb-i-1)+")");
			if(skipFirst && i==0){
				System.out.println("   Skip clustering for first level.");
				SHPUtil.saveSHP(data.fs, outPath+file+"_" + (nb-i-1) + ".shp", data.ft.getCoordinateReferenceSystem());
				continue;
			}
			System.out.println("   Initial size: " + data.fs.size());

			Quadtree index = new Quadtree();
			agg.index = index;
			new Clustering<Feature>().perform(
					data.fs,
					new FeatureDistance(projected),
					resolutions[i] * factor,
					agg,
					false,
					new FeatureClusteringIndex(data.fs, index)
					);
			System.out.println("   Final size: " + data.fs.size());

			SHPUtil.saveSHP(data.fs, outPath+file+"_" + (nb-i-1) + ".shp", data.ft.getCoordinateReferenceSystem());
		}
		System.out.println("Generalisation done");
	}
}
