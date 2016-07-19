package org.opencarto.algo.triangulation;

import com.vividsolutions.jts.geom.Coordinate;

public class TPointFactoryImpl implements TPointFactory {

	@Override
	public TPoint create(Coordinate c) {
		return new TPointImpl(c);
	}

}
