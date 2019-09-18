package org.opencarto.algo.line;

import java.util.Collection;

import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;

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

		/*
		for(int sigmaM : new int[]{100,200,400,600,800,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,15000,20000,25000,50000}){
			System.out.println(sigmaM);
			ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/nuts_gene_data/nuts_2013/100k/NUTS_BN_100K_2013_LAEA.shp", 3035).fs;
			for(Feature f : fs){
				LineString ls = (LineString) JTSGeomUtil.getGeometries(f.getGeom()).iterator().next();
				if(ls.isClosed()) continue;
				//System.out.println(f.id);
				try {
					f.setGeom( GaussianSmoothing.get(ls, sigmaM) );
					//System.out.println("OK!");
				} catch (Exception e) {
					//System.out.println("NOK! "+e.getMessage());
					e.printStackTrace();
				}
			}
			SHPUtil.saveSHP(fs, "/home/juju/Bureau/gauss/gauss"+sigmaM+".shp");
		}
		 */		

		WKTFileReader wfr = new WKTFileReader("src/test/resources/testdata/plane.wkt", new WKTReader());
		Collection<?> gs = wfr.read();
		LineString line = (LineString) gs.iterator().next();

		//LineString ls = new WKTReader().read();
		for(int sigmaM : new int[]{100,200,400,600,800,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000,15000,20000,25000,50000}){
			//System.out.println(sigmaM);
			LineString ls_ = GaussianSmoothing.get(line, sigmaM/100, 0.1);
			System.out.println(ls_);
		}

		System.out.println("End");
	}

}
