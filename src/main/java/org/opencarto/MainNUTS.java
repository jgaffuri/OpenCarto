/**
 * 
 */
package org.opencarto;

import org.geotools.factory.CommonFactoryFinder;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.GeoJSONUtil;
import org.opencarto.io.SHPUtil;
import org.opencarto.io.SHPUtil.SHPData;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;

/**
 * @author julien Gaffuri
 *
 */
public class MainNUTS {
	private static int epsgCode = 4258; //4937

	private static String outPath = "H:desktop/nutsout/";
	private static String[] scales = new String[]{"01M","03M","10M","20M","60M"};
	/*private static String regions = "NUTS_RG_01M_2013.shp";
	private static String borders = "NUTS_BN_01M_2013.shp";*/
	//private static String outPath = "data/tmp/";

	public static void main(String[] args) {
		System.out.println("Start");

		decomposeByLevel();
		//integrate();

		//SHPData data = SHPUtil.loadSHP(folderPath+regions, epsgCode);
		//ProjectionUtil.toWebMercator(data.fs);
		//SHPUtil.saveSHP(data.fs, outPath, "WM.shp");

		System.out.println("Done.");
	}

	private static void decomposeByLevel(){
		for(String scale : scales)
			for(int level = 0; level<=3; level++) {
			decomposeByLevel(level, "RG", scale);
			decomposeByLevel(level, "BN", scale);
		}
	}
	private static void decomposeByLevel(int level, String type, String scale) {
		FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		PropertyIsEqualTo fil = ff.equals( ff.property("STAT_LEVL_"), ff.literal(level) );

		SHPData data = SHPUtil.loadSHP("data/"+"NUTS_2013_"+scale+"_SH/"+"NUTS_"+type+"_"+scale+"_2013.shp", epsgCode, fil);

		//remove useless attributes
		for(Feature f : data.fs){
			f.getProperties().keySet().remove("SHAPE_AREA");
			f.getProperties().keySet().remove("STAT_LEVL_");
			f.getProperties().keySet().remove("SHAPE_LEN");
		}

		//SHPUtil.saveSHP(data.fs, outPath+scale+"_SH/", scale+"_"+type+"_LEV"+level+".shp");
		GeoJSONUtil.toGeoJSON(data.fs, outPath, scale+"_"+type+"_LEV"+level+".json");
	}

	/*private static void integrate(){
		for(int level = 0; level<=3; level++) {
			integrate(level, "RG");
			integrate(level, "BN"); }
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
	}*/

}
