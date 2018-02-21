/**
 * 
 */
package org.opencarto;

import java.io.File;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.transfoengine.tesselationGeneralisation.ATesselation;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.DefaultTesselationGeneralisation;
import org.opencarto.util.JTSGeomUtil;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOGeneXM {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGeneXM.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		GraphBuilder.LOGGER.setLevel(Level.WARN);
		DefaultTesselationGeneralisation.LOGGER.setLevel(Level.WARN);
		ATesselation.LOGGER.setLevel(Level.WARN);

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";

		for(double s : new double[]{1,3,10,20,60}) {

			final CartographicResolution res = new CartographicResolution(s*1e6);

			Collection<Feature> fs, fs_;

			LOGGER.info("Load data");
			final int epsg = 3857; final String rep="serbia"; String inFile = basePath+"serbia/NUTS_3_serbia_WM.shp";

			fs = SHPUtil.loadSHP(inFile, epsg).fs;
			for(Feature f : fs) for(String id : new String[] {"NUTS_CODE","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);

			fs_ = Partition.runRecursively(new Operation() {
				public void run(Partition p) {
					LOGGER.info(p);
					MorphologicalAnalysis.removeNarrowGapsTesselation(p.getFeatures(), res.getSeparationDistanceMeter(), 5, 1e-5);

					ATesselation t = new ATesselation(p.getFeatures(), p.getEnvelope());
					for(AUnit uAg : t.aUnits) uAg.setId(uAg.getObject().id);

					try {
						DefaultTesselationGeneralisation.run(t, null, res, outPath+ rep);
					} catch (Exception e) { e.printStackTrace(); }
					p.features = t.getUnits(epsg);
				}}, fs, 1000000, 5000, false);
			for(Feature f : fs_) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
			SHPUtil.saveSHP(fs_, outPath+ rep+"/", "NUTS_3_serbia_"+(int)s+"M_WM.shp");

		}

		LOGGER.info("End");
	}

}
