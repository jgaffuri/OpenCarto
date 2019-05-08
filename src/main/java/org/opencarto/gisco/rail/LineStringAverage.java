/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * 
 * Build a line representing the average of two lines.
 * This can be usefull when both lines are very similar and an aggregated version is needed.
 * It is simpler than computing a squeletton based central line.
 * The line similarity can be found with the hausdorf distance or (if both have same initial/final points) with surface elongation measure (Elongation.getWidthApproximation).
 * 
 * @author julien Gaffuri
 *
 */
public class LineStringAverage {

	public static LineString get(LineString ls1, LineString ls2) { return get(ls1, ls2, 0.5); }

	public static LineString get(LineString ls1, LineString ls2, double weight) {
		Coordinate[] cs = get(ls1.getCoordinates(), ls1.getLength(), ls2.getCoordinates(), ls2.getLength(), weight);
		return ls1.getFactory().createLineString(cs);
	}

	public static Coordinate[] get(Coordinate[] cs1, double d1, Coordinate[] cs2, double d2, double weight) {

		//compute distances if necessary
		if(d1 <=0 ) d1 = new GeometryFactory().createLineString(cs1).getLength();
		if(d2 <=0 ) d2 = new GeometryFactory().createLineString(cs2).getLength();

		//initialise output
		ArrayList<Coordinate> cs = new ArrayList<>();
		if(cs1.length == 0 || cs2.length == 0) return cs.toArray(new Coordinate[cs.size()]);

		Coordinate c1=cs1[0], c2=cs2[0]; //current points considered in both lines

		//add first point
		cs.add(new Coordinate( c1.x*weight + c2.x*(1-weight), c1.y*weight + c2.y*(1-weight) ));
		int i1=1, i2=1;

		//go step by step
		while(i1 < cs1.length && i2 < cs2.length) {

			//new candidate points on both line
			Coordinate c1_ = cs1[i1];
			Coordinate c2_ = cs1[i2];

			//compute relative progression on both lines
			double d1_ = c1.distance(c1_ );
			double s1_ = d1_ / d1;
			double d2_ = c2.distance(c2_ );
			double s2_ = d2_ / d2;

			//check which progression is the smallest
			if(s1_ == s2_) {
				c1=c1_; c2=c2_;
				i1++; i2++;
			} else if(s1_ > s2_) {
				c2=c2_;
				i2++;
				c1=new Coordinate(
						c1.x + s2_ * d1 * (c1_.x-c1.x) /d1_,
						c1.y + s2_ * d1 * (c1_.y-c1.y) /d1_ );
			} else {
				c1=c1_;
				i1++;
				c2=new Coordinate(
						c2.x + s1_ * d2 * (c2_.x-c2.x) /d2_,
						c2.y + s1_ * d2 * (c2_.y-c2.y) /d2_ );
			}

			//add new point
			cs.add(new Coordinate( c1.x*weight + c2.x*(1-weight), c1.y*weight + c2.y*(1-weight) ));

		}

		return cs.toArray(new Coordinate[cs.size()]);
	}

	
	public static void main(String[] args) {
		
	}
	
}
