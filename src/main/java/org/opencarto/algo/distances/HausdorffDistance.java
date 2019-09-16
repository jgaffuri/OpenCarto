/**
 * 
 */
package org.opencarto.algo.distances;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.distance.DistanceOp;

/**
 * 
 * Compute the Hausdorff distance between two lines.
 * @see <a href="https://en.wikipedia.org/wiki/Hausdorff_distance">https://en.wikipedia.org/wiki/Hausdorff_distance</a>
 * 
 * @author julien Gaffuri
 *
 */
public class HausdorffDistance {

	//the input lines
	private LineString g0, g1;
	public LineString getGeom0() { return g0; }
	public LineString getGeom1() { return g1; }

	public HausdorffDistance(LineString g0, LineString g1) {
		this.g0 = g0;
		this.g1 = g1;
	}

	private double distance = -1;
	/**
	 * @return The hausdorff distance @see <a href="https://en.wikipedia.org/wiki/Hausdorff_distance">https://en.wikipedia.org/wiki/Hausdorff_distance</a>
	 */
	public double getDistance() {
		if(this.distance < 0) compute();
		return this.distance;
	}

	Coordinate c0 = null, c1 = null;
	/**
	 * @return The coordinate of the first geometry where the hausdorff distance is reached.
	 */
	public Coordinate getC0() {
		if(c0 == null) compute();
		return this.c0;
	}
	/**
	 * @return The coordinate of the second geometry where the hausdorff distance is reached.
	 */
	public Coordinate getC1() {
		if(c1 == null) compute();
		return this.c1;
	}


	/**
	 * Compute the Haudorff distance: The max of both max/min distances.
	 */
	private void compute() {

		//compute two parts
		DistanceOp dop01 = compute_(this.g0, this.g1);
		double d01 = dop01.distance();
		DistanceOp dop10 = compute_(this.g1, this.g0);
		double d10 = dop10.distance();

		//get the max and set result
		if(d01>d10) {
			this.distance = d01;
			Coordinate[] cs = dop01.nearestPoints();
			this.c0 = cs[0];
			this.c1 = cs[1];
		} else {
			this.distance = d10;
			Coordinate[] cs = dop10.nearestPoints();
			this.c0 = cs[1];
			this.c1 = cs[0];
		}
	}

	/**
	 * When moving on lineA, computes all shortest distances to lineB.
	 * Return the maximum of these shortest distances.
	 * 
	 * @param lineA
	 * @param lineB
	 * @return
	 */
	private static DistanceOp compute_(LineString lineA, LineString lineB) {
		DistanceOp dopMax = null;
		//go through lineA vertices
		for(Coordinate cA : lineA.getCoordinates()) {
			//find the shortest distance to lineB
			DistanceOp dop = new DistanceOp(lineB.getFactory().createPoint(cA), lineB);
			if(dopMax == null || dop.distance() > dopMax.distance())
				dopMax = dop;
		}
		return dopMax;
	}

	@Override
	public String toString() {
		return "Dist="+getDistance()+" c0="+getC0()+" c1="+getC1();
	}


	/*
	public static void main(String[] args) {
		System.out.println("Test");

		{
			System.out.println("----");
			HausdorffDistance hd = new HausdorffDistance(
					JTSGeomUtil.createLineString(0,0, 100,0, 200,20),
					JTSGeomUtil.createLineString(0,0, 100,0, 200,20)
					);
			System.out.println(hd.getGeom0());
			System.out.println(hd.getGeom1());
			System.out.println(hd);
		}

		{
			System.out.println("----");
			HausdorffDistance hd = new HausdorffDistance(
					JTSGeomUtil.createLineString(0,10, 100,10, 200,30),
					JTSGeomUtil.createLineString(0,0, 100,0, 200,20)
					);
			System.out.println(hd.getGeom0());
			System.out.println(hd.getGeom1());
			System.out.println(hd);
		}

		{
			System.out.println("----");
			HausdorffDistance hd = new HausdorffDistance(
					JTSGeomUtil.createLineString(0,0, 100,0, 200,20),
					JTSGeomUtil.createLineString(0,20, 100,20)
					);
			System.out.println(hd.getGeom0());
			System.out.println(hd.getGeom1());
			System.out.println(hd);
		}

		{
			System.out.println("----");
			HausdorffDistance hd = new HausdorffDistance(
					JTSGeomUtil.createLineString(0,20, 100,20),
					JTSGeomUtil.createLineString(0,0, 100,0, 200,20)
					);
			System.out.println(hd.getGeom0());
			System.out.println(hd.getGeom1());
			System.out.println(hd);
		}


		System.out.println("----");
		System.out.println("End");
	}
	 */

}
