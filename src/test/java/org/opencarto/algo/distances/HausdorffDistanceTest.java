package org.opencarto.algo.distances;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;

/**
 * @author Julien Gaffuri
 *
 */
public class HausdorffDistanceTest extends TestCase {
	//TODO add tests from DiscreteHausdorffDistance

	private WKTReader wr = new WKTReader();

	public HausdorffDistanceTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(HausdorffDistanceTest.class);
	}

	public void testNull() throws Exception {
		Logger.getLogger(HausdorffDistance.class.getName()).setLevel(Level.OFF);
		HausdorffDistance hd = new HausdorffDistance(null, wr.read("LINESTRING(0 0, 100 0)"));
		assertNull(hd.getC0());
		assertNull(hd.getC1());
		assertTrue(Double.isNaN(hd.getDistance()));
	}
	public void testEmpty() throws Exception {
		Logger.getLogger(HausdorffDistance.class.getName()).setLevel(Level.OFF);
		HausdorffDistance hd = new HausdorffDistance(wr.read("LINESTRING EMPTY"), wr.read("LINESTRING(0 0, 100 0)"));
		assertNull(hd.getC0());
		assertNull(hd.getC1());
		assertTrue(Double.isNaN(hd.getDistance()));
	}

	public void test1() throws Exception {
		HausdorffDistance hd = new HausdorffDistance(
				wr.read("LINESTRING(0 0, 100 0, 200 20)"),
				wr.read("LINESTRING(0 0, 100 0, 200 20)")
				);
		assertEquals(hd.getDistance(), 0.0);
	}

	public void test2() throws Exception {
		HausdorffDistance hd = new HausdorffDistance(
				wr.read("LINESTRING(0 10, 100 10, 200 30)"),
				wr.read("LINESTRING(0 0, 100 0, 200 20)")
				);
		assertEquals(hd.getDistance(), 10.0);
		assertEquals(hd.getC0().distance(new Coordinate(0, 10)), 0.0);
		assertEquals(hd.getC1().distance(new Coordinate(0, 0)), 0.0);
	}

	public void test3() throws Exception {
		HausdorffDistance hd = new HausdorffDistance(
				wr.read("LINESTRING(0 0, 100 0, 200 20)"),
				wr.read("LINESTRING(0 20, 100 20)")
				);
		assertEquals(hd.getDistance(), 100.0);
		assertEquals(hd.getC0().distance(new Coordinate(200, 20)), 0.0);
		assertEquals(hd.getC1().distance(new Coordinate(100, 20)), 0.0);
	}

	public void test4() throws Exception {
		HausdorffDistance hd = new HausdorffDistance(
				wr.read("LINESTRING(0 20, 100 20)"),
				wr.read("LINESTRING(0 0, 100 0, 200 20)")
				);
		assertEquals(hd.getDistance(), 100.0);
		assertEquals(hd.getC0().distance(new Coordinate(100, 20)), 0.0);
		assertEquals(hd.getC1().distance(new Coordinate(200, 20)), 0.0);
	}

}
