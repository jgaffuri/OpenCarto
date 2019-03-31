/**
 * 
 */
package org.opencarto.processes;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class DefaultGeneralisation<T extends Feature> extends GeneralisationProcess<T> {
	protected boolean withClustering = true;

	public DefaultGeneralisation(){}
	public DefaultGeneralisation(boolean withClustering){ this.withClustering = withClustering; }

	public void perform(ArrayList<T> fs, ZoomExtend zs){
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

			if(!withClustering) continue;

			//make group generalisation
			//new Clustering<T>().perform(fs, new CentroidDistance(), res, new BufferAggregation(res, res*0.5, 5, res*0.5, z), false);
		}
	}

}
