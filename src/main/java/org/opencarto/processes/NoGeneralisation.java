/**
 * 
 */
package org.opencarto.processes;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;

/**
 * @author julien Gaffuri
 *
 */
public class NoGeneralisation<T extends Feature> extends GeneralisationProcess<T> {

	public void perform(ArrayList<T> fs, ZoomExtend zs){
		System.out.println("Copy geometries (no generalisation)");
		for(int z=zs.max; z>=zs.min; z--)
			for(Feature f: fs)
				f.setGeom(f.getGeom(), z);
	}

}
