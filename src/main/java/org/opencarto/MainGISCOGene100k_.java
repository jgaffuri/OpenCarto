/**
 * 
 */
package org.opencarto;

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
public class MainGISCOGene100k_ {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOGene100k_.class.getName());

	public static void main(String[] args) {
		LOGGER.info("Start");

		GraphBuilder.LOGGER.setLevel(Level.WARN);
		DefaultTesselationGeneralisation.LOGGER.setLevel(Level.WARN);
		ATesselation.LOGGER.setLevel(Level.WARN);

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";

		final CartographicResolution res = new CartographicResolution(1e6);

		Collection<Feature> fs, fs_;

		LOGGER.info("Load data");
		//final int epsg = 3035; final String rep="test"; String inFile = basePath+"test/test.shp";
		final int epsg = 3857; final String rep="100k_1M/commplus"; String inFile = basePath+"commplus/COMM_PLUS_100k_WM.shp";

		fs = SHPUtil.loadSHP(inFile, epsg).fs;
		for(Feature f : fs) for(String id : new String[] {"NUTS_ID","COMM_ID","idgene","GISCO_ID"}) if(f.getProperties().get(id) != null) f.id = ""+f.getProperties().get(id);
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

				for(Feature f : p.getFeatures()) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));

				//SHPUtil.saveSHP(p.getFeatures(), outPath+ rep+"/", "Z_out_"+p.getCode()+".shp");
				//}}, fs, 5000000, 25000);
			}}, fs, 3000000, 15000, false);
		LOGGER.info("Save");
		SHPUtil.saveSHP(fs_, outPath+ rep+"/", "out___.shp");



		LOGGER.info("End");
	}

	/*
	public static HashMap<String,Double> loadNutsArea100k(){
		String inputPath = "/home/juju/Bureau/nuts_gene_data/nuts_2013/100k/NUTS_RG_LVL3_100K_2013_LAEA.shp";
		ArrayList<Feature> fs = SHPUtil.loadSHP(inputPath,3035).fs;
		HashMap<String,Double> out = new HashMap<String,Double>();
		for(Feature f : fs)
			out.put(""+f.getProperties().get("NUTS_ID"), f.getGeom().getArea());
		return out;
	}*/

}
