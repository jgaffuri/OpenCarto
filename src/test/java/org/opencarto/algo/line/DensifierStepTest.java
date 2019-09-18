package org.opencarto.algo.line;

import java.util.Collection;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;

/**
 * @author Julien Gaffuri
 *
 */
public class DensifierStepTest extends TestCase {
	private final WKTReader wr = new WKTReader();

	public DensifierStepTest(String name) { super(name); }


	public static void main(String[] args) {
		junit.textui.TestRunner.run(DensifierStepTest.class);
	}

	public void test0() throws Exception{
		Geometry g = wr.read("LINESTRING(0 0, 1 0)");
		for(double step : new double[] {0.1, 0.05, 0.5, 0.7, 0.005}) {
			Geometry g_ = DensifierStep.densify(g, step);
			assertEquals(g.getLength(), g_.getLength());
			assertEquals((int)(1.0/step)+1, g_.getNumPoints());
		}
	}

	public void test1() throws Exception{
		Geometry g = DensifierStep.densify(wr.read("LINESTRING(0 0, 1 1)"), 0.1);
		assertEquals(Math.sqrt(2), g.getLength());
		assertEquals(15, g.getNumPoints());
	}

	public void test2() throws Exception{
		WKTFileReader wfr = new WKTFileReader("src/test/resources/testdata/plane.wkt", new WKTReader());
		Collection<?> gs = wfr.read();
		Geometry g = (Geometry) gs.iterator().next();
		Geometry g_ = DensifierStep.densify(g, 0.1);

		assertTrue(g.getLength()>g_.getLength());
		assertEquals(2612, g_.getNumPoints());
	}

}
