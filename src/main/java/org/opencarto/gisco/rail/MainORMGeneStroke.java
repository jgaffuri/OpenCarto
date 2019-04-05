/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.opencarto.algo.graph.stroke.Stroke;
import org.opencarto.algo.graph.stroke.StrokeAnalysis;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.ProjectionUtil;

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
		//ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"orm/shp_SE/lines_LAEA.shp").fs;

		//detect issues
		detectIssues(tracks);
		//TODO run correct?

		LOGGER.info("Build strokes");
		Collection<Stroke> sts = new StrokeAnalysis(tracks, false).run(0.6).getStrokes();

		LOGGER.info("Save strokes");
		SHPUtil.saveSHP(sts, basePath+"out/strokes.shp", ProjectionUtil.getETRS89_LAEA_CRS());




		//TODO define and use importance criteria. Use it in salience definition (for both connections and strokes (representative))
		Comparator<Feature> comp = new Comparator<Feature>() {
			@Override
			public int compare(Feature f1, Feature f2) {
				return 0;
			}
		};


		//TODO design overall generalisation algorithm based on:
		// selection first, based on connectivity + stroke + faces?
		// collapse, based on narrow faces



		System.out.println("End");
	}


	
	private static void detectIssues(ArrayList<Feature> sections) {
		//TODO build spatial index
		for(Feature section : sections) {
			//TODO get all sections intersecting

		}
	}

}
