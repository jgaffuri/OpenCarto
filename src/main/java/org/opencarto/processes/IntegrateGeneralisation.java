/**
 * 
 */
package org.opencarto.processes;

import java.util.ArrayList;

import org.opencarto.algo.resolutionise.Resolutionise;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.util.Util;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class IntegrateGeneralisation<T extends Feature> extends GeneralisationProcess<T> {

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
				f.setGeom(Resolutionise.get(geom, res), z);
			}
		}
	}

}
