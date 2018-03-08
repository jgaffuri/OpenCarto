package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.CSVUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;

public class MainTests {

	public static void main(String[] args) {
		System.out.println("Start");

		String basePath = "/home/juju/Bureau/nuts_gene_data/";

		System.out.println("Load data");
		//Collection<Feature> fs = SHPUtil.loadSHP(basePath+"commplus/COMM_PLUS_clean.shp").fs;
		Collection<Feature> fs = SHPUtil.loadSHP(basePath+"out/100k_1M/commplus/COMM_PLUS_WM_1M_1.shp").fs;

		/*
		System.out.println(fs.size());
		System.out.println(FeatureUtil.getNumberVertices(fs));

		System.out.println("Compute id check");
		HashMap<String, Integer> cnts = FeatureUtil.checkIdentfier(fs, "GISCO_ID");
		System.out.println(cnts);
		 */

		final CartographicResolution res = new CartographicResolution(1e6);
		ArrayList<Map<String, Object>> data = analysePolygonsSizes(fs, res.getPerceptionSizeSqMeter());
		CSVUtil.save(data, "/home/juju/Bureau/", "area_analysis.csv");

		System.out.println("End");
	}	


	//considering multi/polygonal features, get the patches that are smallest than an area threshold
	public static ArrayList<Map<String, Object>> analysePolygonsSizes(Collection<Feature> fs, double areaThreshold) {
		ArrayList<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
		for(Feature f : fs) {
			Collection<Geometry> polys = JTSGeomUtil.getGeometries( JTSGeomUtil.keepOnlyPolygonal(f.getGeom()) );
			for(Geometry poly : polys) {
				double area = poly.getArea();
				if( area > areaThreshold ) continue;
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", f.id);
				m.put("area", area);
				m.put("position", poly.getCentroid().getCoordinate());
				out.add(m);
			}
		}
		return out;
	}


}
