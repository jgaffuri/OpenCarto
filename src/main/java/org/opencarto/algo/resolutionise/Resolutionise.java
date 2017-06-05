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
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

/**
 * @author julien Gaffuri
 *
 */
public class Resolutionise {
	public Geometry punctual = null;
	public Geometry linear = null;
	public Geometry aeral = null;

	public Resolutionise(Geometry g, double resolution){
		GeometryFactory gf = g.getFactory();

		if(g instanceof Point){
			punctual = gf.createPoint(get(g.getCoordinate(), resolution));
		} else if(g instanceof MultiPoint) {
			punctual = gf.createMultiPoint( removeDuplicates( get(g.getCoordinates(), resolution) ));
		} else if(g instanceof LineString) {
			Coordinate[] cs = removeConsecutiveDuplicates(get(g.getCoordinates(), resolution));
			if(cs.length == 1) {
				punctual = gf.createPoint(cs[0]);
				//} else if(cs.length == 2 || ( cs.length == 3 && samePosition(cs[0],cs[2]) )) {
				//	linear = gf.createLineString(new Coordinate[]{cs[0],cs[1]});
			} else {
				linear = gf.createLineString(cs);
				//linear = linear.union(linear);
				//linear = linear.intersection(linear);
				//linear = linear.intersection(linear.getEnvelope());

				LineMerger merger = new LineMerger();
				merger.add(linear);
				linear = gf.buildGeometry( merger.getMergedLineStrings() );

				/*LineMerger merger = new LineMerger();
				Coordinate c1, c0 = cs[0];
				for(int i=1; i<cs.length; i++){
					//add segments one by one
					c1 = cs[i];
					LineString ls = gf.createLineString(new Coordinate[]{c0,c1});
					ls.normalize();
					merger.add(ls);
					c0=c1;
				}
				linear = gf.buildGeometry( merger.getMergedLineStrings() );
				linear = linear.intersection(linear);*/
			}
		} else if(g instanceof MultiLineString) {
			MultiLineString g_ = (MultiLineString)g;
			LineMerger merger = new LineMerger();
			for(int i=0; i<g_.getNumGeometries(); i++){
				LineString ls = (LineString)g_.getGeometryN(i);
				Resolutionise res = new Resolutionise(ls, resolution);
				if(res.punctual!=null) punctual = punctual==null? res.punctual : punctual.union(res.punctual);
				if(res.linear!=null) merger.add(res.linear);
			}
			linear = gf.buildGeometry( merger.getMergedLineStrings() );
		} else if(g instanceof Polygon) {
			System.out.println("Resolutionise non implemented yet for Polygon");
		} else if(g instanceof MultiPolygon) {
			System.out.println("Resolutionise non implemented yet for MultiPolygon");
		} else {
			System.out.println("Resolutionise non implemented yet for geometry type: "+g.getGeometryType());
		}
	}

	//return result as a geometry collection
	public Geometry getGeometryCollection() {
		return null;
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

	public static Coordinate[] removeDuplicates(Coordinate[] cs){
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

	public static Coordinate[] removeConsecutiveDuplicates(Coordinate[] cs){
		ArrayList<Coordinate> cs_ = new ArrayList<Coordinate>();
		Coordinate cPrev = null;
		for(Coordinate c : cs){
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

		/*/linestring
		LineString ls;
		ls = gf.createLineString(new Coordinate[] {new Coordinate(107.4, 502.78), new Coordinate(117.4, 500), new Coordinate(487.4, 1402.78)});
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,1).linear);
		System.out.println(new Resolutionise(ls,10).linear);
		System.out.println(new Resolutionise(ls,100).linear);
		System.out.println(new Resolutionise(ls,1000).punctual);
		System.out.println("-------");
		ls = gf.createLineString(new Coordinate[] {new Coordinate(107.4, 502.78), new Coordinate(117.4, 504), new Coordinate(120.4, 490), new Coordinate(107.4, 504)});
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,1).linear);
		System.out.println(new Resolutionise(ls,10).linear);
		System.out.println(new Resolutionise(ls,100).punctual);
		System.out.println(new Resolutionise(ls,1000).linear);
		System.out.println("-------");
		ls = gf.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1000,509), new Coordinate(1000, 500), new Coordinate(0, 1)});
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,10).linear);
		System.out.println(new Resolutionise(ls,100).linear);
		System.out.println("-------");
		ls = gf.createLineString(new Coordinate[] {new Coordinate(0, 1), new Coordinate(1000,1), new Coordinate(1000, 0), new Coordinate(1, 0), new Coordinate(1, -100), new Coordinate(0, -100), new Coordinate(0, 0)});
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,1).linear);
		System.out.println(new Resolutionise(ls,10).linear);
		System.out.println(new Resolutionise(ls,100).linear);*/

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
