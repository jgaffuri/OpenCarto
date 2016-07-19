package org.opencarto.algo.triangulation;

public class TSegmentFactoryImpl implements TSegmentFactory {

	@Override
	public TSegment create(TPoint point1, TPoint point2) {
		return new TSegmentImpl(point1, point2);
	}

}
