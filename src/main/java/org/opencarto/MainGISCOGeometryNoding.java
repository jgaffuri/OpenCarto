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
		//lineCol.add( JTSGeomUtil.createLineString(0,2, 0,0) );
		//lineCol.add( JTSGeomUtil.createLineString(0,2, 0.000000000000000055,1, 1,0) );

		lineCol.add( JTSGeomUtil.createLineString(60.713144858, 81.730685917, 60.713144858, 81.7306859163, 80.9437183817, 03.6479523852) );
		lineCol.add( JTSGeomUtil.createLineString(61.5657030023, 49.4736821207, 60.713144858, 81.730685917, 60.713144858, 81.730685917) );

		//LINESTRING ( 60.713144858 81.7306859163, 80.9437183817 03.6479523852 )
		//LINESTRING ( 61.5657030023 49.4736821207, 60.713144858 81.730685917 )
		//[ (60.713144858, 81.7306859163, NaN) ]
//60.713144858 81.730685917
		
		Geometry union = new GeometryFactory().buildGeometry(lineCol).union();

		System.out.println(union);

		//detect noding pb
		//correct noding pb

	}

}
