package org.opencarto;

import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Polygon;

public class TestInterRounding {

	public static void main(String[] args) {

		Polygon cell1 = JTSGeomUtil.createPolygon(0,0, 100,0, 100,100, 0,100, 0,0);
		Polygon cell2 = JTSGeomUtil.createPolygon(100,0, 200,0, 200,100, 100,100, 100,0);

		System.out.println(cell1);
		System.out.println(cell2);

	}

}
