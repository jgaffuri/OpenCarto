package org.opencarto.gisco;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.partitionning.Partition;
import org.opencarto.partitionning.Partition.Operation;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * @author julien Gaffuri
 *
 */
public class MainGISCOResolutionise {
	private final static Logger LOGGER = Logger.getLogger(MainGISCOResolutionise.class.getName());

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final String outPath = basePath+"out/";
		Collection<Feature> fs, fs_;

		double resolution = 0.1;

		LOGGER.info("Load data");
		final int epsg = 3857; final String rep="100k_1M/commplus"; fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_FINAL_WM.shp", epsg).fs;
		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("idgene") != null) f.id = ""+f.getProperties().get("idgene");
			else if(f.getProperties().get("GISCO_ID") != null) f.id = ""+f.getProperties().get("GISCO_ID");

		fs_ = Partition.runRecursively(fs, new Operation() {
			public void run(Partition p) {
				LOGGER.info(p);

				for(Feature f : p.features) {
					MultiPolygon mp = (MultiPolygon) f.getGeom();
					//MultiPolygon mpRes = Resolutionise.get(mp);

				}

			}}, 3000000, 15000, false);
		LOGGER.info("Save");
		for(Feature f : fs_) f.setGeom(JTSGeomUtil.toMulti(f.getGeom()));
		SHPUtil.saveSHP(fs_, outPath+ rep+"/resol.shp");

		System.out.println("End");
	}

}
