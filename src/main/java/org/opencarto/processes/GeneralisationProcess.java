package org.opencarto.processes;

import java.util.ArrayList;

import org.opencarto.algo.base.DouglasPeuckerRamerFilter;
import org.opencarto.algo.measure.Size;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.util.ProjectionUtil;

import com.vividsolutions.jts.geom.Geometry;

public abstract class GeneralisationProcess<T extends Feature> {

	public abstract void perform(ArrayList<T> fs, ZoomExtend zs);



	public static double getResolution(int z){
		return ProjectionUtil.getPixelSizeEqu(z);
	}

	public static Geometry pre(Geometry geom, double res) {
		//if it has no diagonal longer than the resolution, replace with point
		if(!Size.hasDiagonalLongerThan(geom, res))
			return geom.getCentroid();
		//else, filter
		return DouglasPeuckerRamerFilter.get(geom, res);
	}

}
