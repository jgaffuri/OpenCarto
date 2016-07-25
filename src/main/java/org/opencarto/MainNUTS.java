/**
 * 
 */
package org.opencarto;

import org.geotools.factory.CommonFactoryFinder;
import org.opencarto.io.SHPUtil;
import org.opencarto.io.SHPUtil.SHPData;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

/**
 * @author julien Gaffuri
 *
 */
public class MainNUTS {
	private static int epsgCode = 4258;
	private static String folderPath = "data/NUTS_2013_01M_SH/";
	private static String regions = "NUTS_RG_01M_2013.shp";
	private static String borders = "NUTS_BN_01M_2013.shp";
	private static String outPath = "H:/desktop/nuts/";

	public static void main(String[] args) {
		System.out.println("Start.");

		decomposeByLevel();

		System.out.println("Done.");
	}

	public static void decomposeByLevel(){
		for(int level = 0; level<=3; level++) { decomposeByLevel(level, "RG"); decomposeByLevel(level, "BN"); }
	}
	public static void decomposeByLevel(int level, String type) {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		PropertyIsEqualTo fil = ff.equals( ff.property("STAT_LEVL_"), ff.literal(level) );
		SHPData data = SHPUtil.loadSHP("data/NUTS_2013_01M_SH/NUTS_"+type+"_01M_2013.shp", epsgCode, fil);
		SHPUtil.saveSHP(data.fs, outPath, "NUTS_"+type+"_"+level+".shp");
	}

}
