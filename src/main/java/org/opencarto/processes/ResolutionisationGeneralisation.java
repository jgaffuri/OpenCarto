/**
 * 
 */
package org.opencarto.processes;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.opencarto.datamodel.MultiScaleFeature;
import org.opencarto.datamodel.ZoomExtend;

import eu.europa.ec.eurostat.eurogeostat.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class ResolutionisationGeneralisation<T extends MultiScaleFeature> extends GeneralisationProcess<T> {

	public void perform(ArrayList<T> fs, ZoomExtend zs){
		for(int z=zs.max; z>=zs.min; z--){
			//get resolution value
			double res = getResolution(z);
			System.out.println("Generalisation: "+z+" (resolution "+Util.round(res, 1)+"m)");

			//get zoom code to base on
			String zBase = z==zs.max? "" : String.valueOf(z+1);

			//make individual generalisation
			for(MultiScaleFeature f: fs){
				Geometry geom = f.getGeom(zBase);
				if(geom==null) continue;
				//TODO
				//f.setGeom(new Resolutionise(geom, res).getGeometryCollection(), z);
			}
		}
	}

}
