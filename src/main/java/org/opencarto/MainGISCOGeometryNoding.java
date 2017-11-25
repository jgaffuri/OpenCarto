package org.opencarto;

import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Polygon;

public class MainGISCOGeometryNoding {

	public static void main(String[] args) {

		//make example on simple geometries + on real geo file

		Polygon poly1 = JTSGeomUtil.createPolygon(0,0, 1,0, 0,1, 0,0);

		//detect noding pb
		//correct noding pb


	}

}
