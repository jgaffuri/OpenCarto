package org.opencarto;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class MainGISCOGeometryNoding {

	public static void main(String[] args) {

		//make example on simple geometries + on real geo file

		LineString l1 = JTSGeomUtil.createLineString(0,0, 1,0, 0,1, 0,0);
		LineString l2 = JTSGeomUtil.createLineString(0,0, 1,0, 0,1, 0,0);


		Collection<Geometry> lineCol = new HashSet<Geometry>();
		Geometry union = new GeometryFactory().buildGeometry(lineCol).union();

		//detect noding pb
		//correct noding pb


	}

}
