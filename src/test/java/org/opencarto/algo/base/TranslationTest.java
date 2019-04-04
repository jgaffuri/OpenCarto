package org.opencarto.algo.base;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;

class TranslationTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TranslationTest.class);
	}

	void test() {
		fail("Not yet implemented");
	}

	public void testEmptyPolygon() throws Exception {
		String geomStr = "POLYGON(EMPTY)";
		new GeometryOperationValidator(
				TranslationResult.getResult(geomStr,1, 1))
		.setExpectedResult(geomStr)
		.test();
	}

}


class TranslationResult{
	private static WKTReader rdr = new WKTReader();

	public static Geometry[] getResult(String wkt, double dx, double dy) throws ParseException {
		Geometry[] ioGeom = new Geometry[2];
		ioGeom[0] = rdr.read(wkt);
		ioGeom[1] = Translation.get(ioGeom[0], dx, dy);
		return ioGeom;
	}
}
