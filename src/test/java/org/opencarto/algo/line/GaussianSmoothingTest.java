package org.opencarto.algo.line;

import java.util.Collection;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;

import eu.europa.ec.eurostat.jgiscotools.algo.line.GaussianLineSmoothing;
import junit.framework.TestCase;

/**
 * @author Julien Gaffuri
 *
 */
public class GaussianSmoothingTest extends TestCase {
	private final WKTReader wr = new WKTReader();

	public GaussianSmoothingTest(String name) { super(name); }

	public static void main(String[] args) {
		junit.textui.TestRunner.run(GaussianSmoothingTest.class);
	}

	public void test1() throws Exception{
		WKTFileReader wfr = new WKTFileReader("src/test/resources/testdata/plane.wkt", wr);
		Collection<?> gs = wfr.read();
		LineString ls = (LineString) gs.iterator().next();
		for(double sigmaM : new double[]{0.001,1,2,4,6,8,10,20,30,40,50,60,70,80,90,100,150,200,100000}){
			LineString ls_ = GaussianLineSmoothing.get(ls, sigmaM, 0.1);
			assertTrue(ls_.getLength()<ls.getLength());
			assertTrue(ls_.getCoordinateN(0).distance(ls.getCoordinateN(0)) == 0);
			assertTrue(ls_.getCoordinateN(ls_.getNumPoints()-1).distance(ls.getCoordinateN(ls.getNumPoints()-1)) == 0);
		}
	}

	public void test2() throws Exception{
		WKTFileReader wfr = new WKTFileReader("src/test/resources/testdata/world.wkt", wr);
		Collection<?> gs = wfr.read();
		for(Object g_ : gs) {
			LineString ls = null;
			if(g_ instanceof Polygon)
				ls = ((Polygon)g_).getExteriorRing();
			else if(g_ instanceof MultiPolygon)
				ls = ((Polygon)((MultiPolygon)g_).getGeometryN(0)).getExteriorRing();
			else
				continue;
			for(double sigmaM : new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1, 100, 100000}){
				LineString ls_ = GaussianLineSmoothing.get(ls, sigmaM, 0.1);
				assertTrue(ls_.getLength()<ls.getLength());
			}
		}
	}


	/*
	public static void main(String[] args) throws Exception {

		String inFile = "E:/dissemination/shared-data/gisco_shp/GISCO.NUTS_2016/NUTS_BN_100K_2016.shp";
		//for(double sigmaM : new double[]{100,200,400,600,800,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,15000,20000,25000,50000}){
		for(double sigmaM : new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1, 100, 100000}){
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

		System.out.println("End");
	}
	 */
}
