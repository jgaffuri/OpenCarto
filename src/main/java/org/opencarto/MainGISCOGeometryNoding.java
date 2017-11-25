package org.opencarto;

import java.util.Collection;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class MainGISCOGeometryNoding {

	public static void main(String[] args) {

		//make example on simple geometries + on real geo file

		Collection<Geometry> lineCol = new HashSet<Geometry>();
		//lineCol.add( JTSGeomUtil.createLineString(0,2, 0,0) );
		//lineCol.add( JTSGeomUtil.createLineString(0,2, 0.000000000000000055,1, 1,0) );
		
		//LINESTRING ( 3225160.713144858 1840681.7306859163, 3225180.9437183817 1840603.6479523852 )
		//LINESTRING ( 3225161.5657030023 1840649.4736821207, 3225160.713144858 1840681.730685917 )
		//[ (3225160.713144858, 1840681.7306859163, NaN) ]

		
		Geometry union = new GeometryFactory().buildGeometry(lineCol).union();

		System.out.println(union);
		
		//detect noding pb
		//correct noding pb

	}

}
