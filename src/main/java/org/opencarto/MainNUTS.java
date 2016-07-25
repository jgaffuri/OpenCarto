/**
 * 
 */
package org.opencarto;

import org.geotools.factory.CommonFactoryFinder;
import org.opencarto.algo.integrate.Integrate;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.io.SHPUtil;
import org.opencarto.io.SHPUtil.SHPData;
import org.opencarto.processes.GeneralisationProcess;
import org.opencarto.util.ProjectionUtil;
import org.opencarto.util.Util;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class MainNUTS {
	private static int epsgCode = 4258; //4937
	private static String folderPath = "data/NUTS_2013_01M_SH/";
	private static String regions = "NUTS_RG_01M_2013.shp";
	private static String borders = "NUTS_BN_01M_2013.shp";
	private static String outPath = "data/tmp/";

	public static void main(String[] args) {
		System.out.println("Start");

		//decomposeByLevel();
		//integrate();

		SHPData data = SHPUtil.loadSHP(folderPath+regions, epsgCode);
		ProjectionUtil.toWebMercator(data.fs);
		SHPUtil.saveSHP(data.fs, outPath, "WM.shp");

		System.out.println("Done.");
	}

	/*private static void decomposeByLevel(){
		for(int level = 0; level<=3; level++) { decomposeByLevel(level, "RG"); decomposeByLevel(level, "BN"); }
	}
	private static void decomposeByLevel(int level, String type) {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		PropertyIsEqualTo fil = ff.equals( ff.property("STAT_LEVL_"), ff.literal(level) );
		SHPData data = SHPUtil.loadSHP(folderPath+"NUTS_"+type+"_01M_2013_WM.shp", epsgCode, fil);
		SHPUtil.saveSHP(data.fs, outPath, "NUTS_"+type+"_"+level+".shp");
	}*/

	private static void integrate(){
		for(int level = 0; level<=3; level++) { integrate(level, "RG"); /*integrate(level, "BN");*/ }
	}
	private static void integrate(int level, String type) {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		PropertyIsEqualTo fil = ff.equals( ff.property("STAT_LEVL_"), ff.literal(level) );
		SHPData data = SHPUtil.loadSHP(folderPath+"NUTS_"+type+"_01M_2013.shp", epsgCode, fil);
		System.out.println(data.fs.size() + " regions loaded.");

		//project
		ProjectionUtil.toWebMercator(data.fs);

		ZoomExtend zs = new ZoomExtend(8,8);
		for(int z=zs.max; z>=zs.min; z--){
			//get resolution value
			double res = GeneralisationProcess.getResolution(z);
			System.out.println("Generalisation: "+z+" (resolution "+Util.round(res, 1)+"m)");

			//make individual generalisation
			for(Feature f: data.fs){
				Geometry geom = f.getGeom();
				if(geom==null) continue;
				f.setGeom( Integrate.perform(geom, res) );
			}
			SHPUtil.saveSHP(data.fs, outPath, "NUTS_"+type+"_"+level+"_z"+z+".shp");
		}
	}

}
