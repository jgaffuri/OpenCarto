package org.opencarto.algo.triangulation;

import org.locationtech.jts.geom.Coordinate;

public interface TPointFactory {

	public TPoint create(Coordinate c);

}
