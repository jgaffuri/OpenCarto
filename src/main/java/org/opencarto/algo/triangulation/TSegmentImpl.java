package org.opencarto.algo.triangulation;

public class TSegmentImpl implements TSegment {

	private TPoint pt1;
	@Override
	public TPoint getPt1() { return this.pt1; }

	private TPoint pt2;
	@Override
	public TPoint getPt2() { return this.pt2; }

	public TSegmentImpl(TPoint pt1, TPoint pt2) {
		this.pt1 = pt1;
		this.pt2 = pt2;
	}

}
