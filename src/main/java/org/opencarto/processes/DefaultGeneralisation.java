/**
 * 
 */
package org.opencarto.processes;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.util.Util;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class DefaultGeneralisation extends GeneralisationProcess<Feature> {

	public void perform(ArrayList<Feature> fs, ZoomExtend zs){
		for(int z=zs.max; z>=zs.min; z--){
			//get resolution value
			double res = getResolution(z);
			System.out.println("Generalisation: "+z+" (resolution "+Util.round(res, 1)+"m)");

			//get zoom code to base on
			String zBase = z==zs.max? "" : String.valueOf(z+1);

			//make individual generalisation
			for(Feature f: fs){
				Geometry geom = f.getGeom(zBase);
				if(geom==null) continue;
				f.setGeom(pre(geom, res), z);
			}

			//make group generalisation
			//new Clustering<T>().perform(fs, new CentroidDistance(), res, new BufferAggregation(res, res*0.5, 5, res*0.5, z), false);
		}
	}

}
