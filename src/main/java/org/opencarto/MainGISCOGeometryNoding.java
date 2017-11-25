package org.opencarto;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class MainGISCOGeometryNoding {

	public static void main(String[] args) {

		//make example on simple geometries + on real geo file

		Collection<Geometry> lineCol = new HashSet<Geometry>();
		lineCol.add( JTSGeomUtil.createLineString(0,0, 1,0, 0,1, 0,0) );
		lineCol.add( JTSGeomUtil.createLineString(1,0, 0.5000000000000001,0.5) );
		Geometry union = new GeometryFactory().buildGeometry(lineCol).union();

		System.out.println(union);
		
		//detect noding pb
		//correct noding pb

	}

}
