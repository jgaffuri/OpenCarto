/**
 * 
 */
package org.opencarto.algo.resolutionise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class Resolutionise {
	public Geometry punctual = null;
	public Geometry linear = null;
	public Geometry aeral = null;

	public Resolutionise(Geometry g, double resolution){
		if(g instanceof Point){
			punctual = new GeometryFactory().createPoint(get(g.getCoordinate(), resolution));
		} else if(g instanceof MultiPoint){
			Coordinate[] cs = removeDuplicate( get(g.getCoordinates(), resolution) );
			punctual = new GeometryFactory().createMultiPoint(cs);
		} else if(g instanceof LineString){
			System.out.println("Resolutionise non implemented yet for LineString");
		}
		System.out.println("Resolutionise non implemented yet for geometry type: "+g.getGeometryType());
	}


	//base functions

	private static boolean samePosition(Coordinate c1, Coordinate c2) { return c1.x==c2.x && c1.y==c2.y; }
	public static Coordinate get(Coordinate c, double resolution){
		return new Coordinate(
				Math.round(c.x/resolution)*resolution,
				Math.round(c.y/resolution)*resolution
				);
	}
	public static Coordinate[] get(Coordinate[] cs, double resolution){
		Coordinate[] cs_ = new Coordinate[cs.length];
		for(int i=0; i<cs.length; i++) cs_[i] = get(cs[i], resolution);
		return cs_;
	}
	public static void apply(Coordinate c, double resolution){
		c.x = Math.round(c.x/resolution)*resolution;
		c.y = Math.round(c.y/resolution)*resolution;
	}
	public static void apply(Coordinate[] cs, double resolution){ for(Coordinate c : cs) apply(c, resolution); }

	public static Coordinate[] removeDuplicate(Coordinate[] cs){
		ArrayList<Coordinate> csSorted = new ArrayList<Coordinate>(Arrays.asList(cs));
		csSorted.sort(new Comparator<Coordinate>() {
			public int compare(Coordinate c1, Coordinate c2) { return c1.x>c2.x?1:c1.y>c2.y?1:0; }
		});
		HashSet<Coordinate> cs_ = new HashSet<Coordinate>();
		Coordinate cPrev = null;
		for(Coordinate c : csSorted){
			if(cPrev==null || !samePosition(c,cPrev)) cs_.add(c);
			cPrev=c;
		}
		return cs_.toArray(new Coordinate[cs_.size()]);
	}

	public static void main(String[] args) {
		//tests
		//TODO extract as true tests

		GeometryFactory gf = new GeometryFactory();

		//points
		/*Point pt;
		pt = gf.createPoint(new Coordinate(107.4, 502.78));
		System.out.println(pt);
		System.out.println(new Resolutionise(pt,1).punctual);
		System.out.println(new Resolutionise(pt,10).punctual);
		System.out.println(new Resolutionise(pt,100).punctual);

		pt = gf.createPoint(new Coordinate(87.5, 502.78));
		System.out.println(pt);
		System.out.println(new Resolutionise(pt,1).punctual);
		System.out.println(new Resolutionise(pt,10).punctual);
		System.out.println(new Resolutionise(pt,100).punctual);*/

		//multipoint
		/*MultiPoint pt;
		pt = gf.createMultiPoint(new Coordinate[] {new Coordinate(107.4, 502.78), new Coordinate(117.4, 500), new Coordinate(487.4, 1402.78)});
		System.out.println(pt);
		System.out.println(new Resolutionise(pt,1).punctual);
		System.out.println(new Resolutionise(pt,10).punctual);
		System.out.println(new Resolutionise(pt,100).punctual);
		System.out.println(new Resolutionise(pt,1000).punctual);*/

	}



	/*
	public static  Geometry get(Geometry g, double resolution) {
		Geometry out = Copy.perform(g);

		apply(out.getCoordinates(), resolution);

		if(out instanceof Point) ;
		else if(out instanceof MultiPoint) ;
		else if(out instanceof LineString) ;
		else if(out instanceof MultiLineString) ;
		else if(out instanceof Polygon) out = out.buffer(0);
		else if(out instanceof MultiPolygon) out = out.buffer(0);

		return out;
	}*/

}
