package org.opencarto.algo.triangulation;

import com.vividsolutions.jts.geom.Coordinate;

public interface TPointFactory {

	public TPoint create(Coordinate c);

}
