package org.opencarto;

import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Polygon;

public class Test {

	public static void main(String[] args) {


		//not OK
		Polygon test = JTSGeomUtil.createPolygon(10,10, 190,10+(1.0/3.0), 190,90, 10,90, 10,10);
		//OK
		//Polygon test = JTSGeomUtil.createPolygon(10,10, 190,10+Math.sqrt(2), 190,90, 10,90, 10,10);
		//OK
		//Polygon test = JTSGeomUtil.createPolygon(10,10, 190,10+Math.PI, 190,90, 10,90, 10,10);

		//conclusion: not exact for some cases, but should be enough for partitioning work from quadtree structure

		System.out.println(test);
		System.out.println(test.getArea());
		System.out.println(test.getLength());

		Polygon cell1 = JTSGeomUtil.createPolygon(0,0, 100,0, 100,100, 0,100, 0,0);
		Polygon cell2 = JTSGeomUtil.createPolygon(100,0, 200,0, 200,100, 100,100, 100,0);
		Polygon inter1 = (Polygon) test.intersection(cell1);
		Polygon inter2 = (Polygon) test.intersection(cell2);

		System.out.println(inter1);
		System.out.println(inter2);
		System.out.println(inter1.getArea()+inter2.getArea());

		Polygon union = (Polygon) inter1.union(inter2);
		//union = (Polygon) union.buffer(0);
		System.out.println(union);
		System.out.println(union.getArea());
		System.out.println(union.getLength());

		System.out.println( union.equals(test) );
		System.out.println( union.symDifference(test) + " " +union.symDifference(test).getArea() );
		System.out.println( test.symDifference(union) + " " +test.symDifference(union).getArea() );

	}

}
