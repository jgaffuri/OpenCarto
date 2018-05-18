/**
 * 
 */
package org.opencarto.gisco;

import java.util.ArrayList;
import java.util.Collection;

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

		LOGGER.info("Build strokes");
		Collection<Stroke> sts = new StrokeAnalysis(tracks, false).run(0.6).getStrokes();

		LOGGER.info("Save strokes");
		SHPUtil.saveSHP(sts, basePath+"out/strokes.shp", ProjectionUtil.getETRS89_LAEA_CRS());

		//TODO take into account attribute in salience computation (for both connections and strokes (representative))

		System.out.println("End");
	}

}
