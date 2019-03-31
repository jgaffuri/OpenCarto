package org.opencarto.processes;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.opencarto.algo.base.DouglasPeuckerRamerFilter;
import org.opencarto.algo.measure.Size;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.util.ProjectionUtil;

/**
 * A generic generalisation process
 * 
 * @author Julien Gaffuri
 *
 * @param <T>
 */
public abstract class GeneralisationProcess<T extends Feature> {

	/**
	 * The generalisation process
	 * 
	 * @param fs The collection of features
	 * @param zs The zoom extend
	 */
	public abstract void perform(ArrayList<T> fs, ZoomExtend zs);



	/**
	 * Computes the resolution for a zoom level.
	 * 
	 * @param z
	 * @return
	 */
	public static double getResolution(int z){
		return ProjectionUtil.getPixelSizeEqu(z);
	}

	/**
	 * Generic generalisation procedure for a single feature, based on the diagonal length
	 * 
	 * @param geom
	 * @param res
	 * @return
	 */
	public static Geometry pre(Geometry geom, double res) {
		//if it has no diagonal longer than the resolution, replace with point
		if(!Size.hasDiagonalLongerThan(geom, res))
			return geom.getCentroid();
		//else, filter
		return DouglasPeuckerRamerFilter.get(geom, res);
	}

}
