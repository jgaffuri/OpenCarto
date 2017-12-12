package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.algo.noding.NodingUtil.NodingIssue;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.CUnitNoding;

import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

public class MainGISCOGeometryNoding {

	public static void main(String[] args) {
		System.out.println("Start");

		System.out.println("Load data");
		String basePath = "/home/juju/Bureau/nuts_gene_data/";
		final int epsg = 3035; ArrayList<Feature> fs = SHPUtil.loadSHP(basePath + "out/100k_1M/comm/out_narrow_gaps_removed.shp", epsg).fs;

		for(Feature f : fs)
			if(f.getProperties().get("NUTS_ID") != null) f.id = ""+f.getProperties().get("NUTS_ID");
			else if(f.getProperties().get("COMM_ID") != null) f.id = ""+f.getProperties().get("COMM_ID");
			else if(f.getProperties().get("ADM0_CODE") != null) f.id = ""+f.getProperties().get("ADM0_CODE");
			else if(f.getProperties().get("ADM0_NAME") != null) f.id = ""+f.getProperties().get("ADM_NAME");

		double nodingResolution = 1e-5;

		System.out.println("Build spatial index for units");
		SpatialIndex index = new STRtree();
		for(Feature f : fs) index.insert(f.getGeom().getEnvelopeInternal(), f);

		for(Feature f : fs) {

			//detect noding issues
			CUnitNoding cst = new CUnitNoding(new AUnit(f), index, nodingResolution);
			cst.computeCurrentValue();
			Collection<NodingIssue> nis = cst.getIssues();

			if(nis.size()>0) System.out.println(f.id+" - "+nis.size());

			//fix them
			//while()
		}

		//save output
		SHPUtil.saveSHP(fs, basePath+ "out/100k_1M/comm/", "out_narrow_gaps_removed_noded.shp");

		System.out.println("End");
	}

}
