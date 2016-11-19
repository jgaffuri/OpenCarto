package org.opencarto.algo.triangulation;

import com.vividsolutions.jts.geom.Coordinate;

public class TPointImpl implements TPoint {

	private Coordinate position;
	public Coordinate getPosition() { return this.position; }

	public TPointImpl(Coordinate position) {
		this.position = position;
	}

}
