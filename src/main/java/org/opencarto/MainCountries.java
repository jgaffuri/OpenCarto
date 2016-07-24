/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.io.SHPUtil;
import org.opencarto.processes.IntegrateGeneralisation;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.description.Description;
import org.opencarto.tiling.description.DescriptionBuilder;
import org.opencarto.tiling.vector.VectorTileBuilder;

/**
 * @author julien Gaffuri
 *
 */
public class MainCountries {

	public static void main(String[] args) {
		String inPath = "/home/juju/Bureau/repoju/data/fish/CNTR_2010_03M_SH/cntr_wm.shp";
		String outPath = "/home/juju/workspace/opencarto-code/client/war/data/countries/";
		int zoomMax = 7;

		ZoomExtend zs = new ZoomExtend(0,zoomMax);

		//load countries
		System.out.println("Load countries in "+inPath);

		ArrayList<Feature> fs = SHPUtil.getFeatures(inPath/*, new String[]{"CNTR_ID","CNTR_AT__1","CNTR_AT_IS"}, null*/);
		System.out.println(fs.size() + " countries loaded.");
		//System.out.println(fs.get(0).props);

		//make generalisation
		new IntegrateGeneralisation().perform(fs, zs);

		//make tiles
		System.out.println("Tiling");
		new Tiling(fs, new VectorTileBuilder(), outPath, zs, false).doTiling();

		//System.out.println("Descriptions");
		Description.export(fs, outPath, new MainCountries().new CountryDescriptionBuilder(), false);

		System.out.println("Done.");
	}

	public class CountryDescriptionBuilder implements DescriptionBuilder {

		@Override
		public String getDescription(Feature f) {
			return (String) f.props.get("CNTR_AT__1");
		}

	}

}
