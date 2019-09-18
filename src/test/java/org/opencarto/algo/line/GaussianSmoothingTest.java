package org.opencarto.algo.line;

import java.util.ArrayList;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.JTSGeomUtil;

import junit.framework.TestCase;

/**
 * @author Julien Gaffuri
 *
 */
public class GaussianSmoothingTest extends TestCase {
	private final WKTReader wr = new WKTReader();

	public GaussianSmoothingTest(String name) { super(name); }

	/*
	public static void main(String[] args) {
		junit.textui.TestRunner.run(DensifierStepTest.class);
	}
	 */





	public static void main(String[] args) throws Exception {

		String inFile = "E:/dissemination/shared-data/gisco_shp/GISCO.NUTS_2016/NUTS_BN_100K_2016.shp";
		//for(double sigmaM : new double[]{100,200,400,600,800,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,15000,20000,25000,50000}){
		for(double sigmaM : new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1}){
			//sigmaM = sigmaM/50000.0;
			System.out.println(sigmaM);
			ArrayList<Feature> fs = SHPUtil.loadSHP(inFile).fs;
			for(Feature f : fs){
				LineString ls = (LineString) JTSGeomUtil.getGeometries(f.getGeom()).iterator().next();
				try {
					f.setGeom( GaussianLineSmoothing.get(ls, sigmaM, 0.001) );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			SHPUtil.saveSHP(fs, "C:/Users/gaffuju/Desktop/gauss/gauss"+sigmaM+".shp", SHPUtil.getCRS(inFile));
		}


		/*
		WKTFileReader wfr = new WKTFileReader("src/test/resources/testdata/plane.wkt", new WKTReader());
		Collection<?> gs = wfr.read();
		LineString line = (LineString) gs.iterator().next();

		//LineString ls = new WKTReader().read();
		for(int sigmaM : new int[]{100,200,400,600,800,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,15000,20000,25000,50000}){
			//System.out.println(sigmaM);
			LineString ls_ = GaussianSmoothing.get(line, sigmaM/100, 0.1);
			System.out.println(ls_);
		}
		 */

		System.out.println("End");
	}

}
