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
import com.vividsolutions.jts.geom.Lineal;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Polygonal;
import com.vividsolutions.jts.geom.Puntal;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

/**
 * @author julien Gaffuri
 *
 */
public class Resolutionise {
	public Puntal puntal = null;
	public Lineal lineal = null;
	public Polygonal polygonal = null;

	public Resolutionise(Geometry g, double resolution){
		GeometryFactory gf = g.getFactory();

		if(g instanceof Point){
			//simply create point with rounded coordinates
			puntal = gf.createPoint(get(g.getCoordinate(), resolution));
		} else if(g instanceof MultiPoint) {
			//remove duplicates from rounded coordinates
			puntal = gf.createMultiPoint( removeDuplicates( get(g.getCoordinates(), resolution) ));
		} else if(g instanceof LineString) {
			//round coordinates and remove consecutive duplicates
			Coordinate[] cs = removeConsecutiveDuplicates(get(g.getCoordinates(), resolution));
			if(cs.length == 1)
				//line shrinked to point
				puntal = gf.createPoint(cs[0]);
			else {
				//generate resolutionised line
				LineMerger merger = new LineMerger();
				merger.add( gf.createLineString(cs).union() );
				lineal = (Lineal) gf.buildGeometry( merger.getMergedLineStrings() );
			}
		} else if(g instanceof MultiLineString) {
			MultiLineString g_ = (MultiLineString)g;
			int nb = g_.getNumGeometries();

			//compute resolusionise of each component
			Resolutionise[] res = new Resolutionise[g_.getNumGeometries()];
			for(int i=0; i<nb; i++)
				res[i] = new Resolutionise(g_.getGeometryN(i), resolution);

			//store puntals
			for(int i=0; i<nb; i++) {
				if(res[i].puntal != null) puntal = puntal==null? res[i].puntal : (Puntal)((Geometry) puntal).union((Geometry) res[i].puntal);
			}

			//use linemerger for lineals
			LineMerger merger = new LineMerger();
			for(int i=0; i<nb; i++)
				merger.add((Geometry) res[i].lineal);
			lineal = (Lineal) gf.buildGeometry( merger.getMergedLineStrings() );
			//union
			lineal = (Lineal) ((Geometry) lineal).union();

			if(lineal instanceof MultiLineString){
				//since lineal components could overlap, do another resolutionise
				g_ = (MultiLineString)lineal;
				nb = g_.getNumGeometries();
				res = new Resolutionise[g_.getNumGeometries()];
				for(int i=0; i<nb; i++)
					res[i] = new Resolutionise(g_.getGeometryN(i), resolution);

				//use linemerger again
				merger = new LineMerger();
				for(int i=0; i<nb; i++)
					merger.add((Geometry) res[i].lineal);
				lineal = (Lineal) gf.buildGeometry( merger.getMergedLineStrings() );
				//union
				lineal = (Lineal) ((Geometry) lineal).union();
			}

			//complement puntal with lineal to ensure puntal does not intersect lineal
			//TODO

		} else if(g instanceof Polygon) {
			LineString er = ((Polygon) g).getExteriorRing();
			Resolutionise resEr = new Resolutionise(er, resolution);
			if(resEr.puntal!=null)
				//polygon shrinked to point
				puntal = resEr.puntal;
			else if (resEr.polygonal==null)
				//polygon shrinked to line only
				lineal = resEr.lineal;
			else {
				//TODO
				//build final polygon
				//make union
				//get components
			}
		} else if(g instanceof MultiPolygon) {
			System.out.println("Resolutionise non implemented yet for MultiPolygon");
		} else {
			System.out.println("Resolutionise non implemented yet for geometry type: "+g.getGeometryType());
		}
	}

	//return result as a geometry collection
	public Geometry getGeometryCollection() {
		Geometry geom = null;
		if(polygonal !=null ) geom = geom==null? (Geometry)polygonal : geom.union((Geometry)polygonal);
		if(lineal !=null ) geom = geom==null? (Geometry)lineal : geom.union((Geometry)lineal);
		if(puntal != null) geom = geom==null? (Geometry) puntal : geom.union((Geometry)puntal);
		return geom;
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
		System.out.println(new Resolutionise(pt,1).puntal);
		System.out.println(new Resolutionise(pt,10).puntal);
		System.out.println(new Resolutionise(pt,100).puntal);

		pt = gf.createPoint(new Coordinate(87.5, 502.78));
		System.out.println(pt);
		System.out.println(new Resolutionise(pt,1).puntal);
		System.out.println(new Resolutionise(pt,10).puntal);
		System.out.println(new Resolutionise(pt,100).puntal);*/

		//multipoint
		/*MultiPoint pt;
		pt = gf.createMultiPoint(new Coordinate[] {new Coordinate(107.4, 502.78), new Coordinate(117.4, 500), new Coordinate(487.4, 1402.78)});
		System.out.println(pt);
		System.out.println(new Resolutionise(pt,1).puntal);
		System.out.println(new Resolutionise(pt,10).puntal);
		System.out.println(new Resolutionise(pt,100).puntal);
		System.out.println(new Resolutionise(pt,1000).puntal);*/

		/*/linestring
		LineString ls;
		ls = gf.createLineString(getCoordsArray(107.4, 502.78, 117.4, 500, 487.4, 1402.78));
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,1).lineal);
		System.out.println(new Resolutionise(ls,10).lineal);
		System.out.println(new Resolutionise(ls,100).lineal);
		System.out.println(new Resolutionise(ls,1000).puntal);
		System.out.println("-------");
		ls = gf.createLineString(getCoordsArray(107.4, 502.78, 117.4, 504, 120.4, 490, 107.4, 503));
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,1).lineal);
		System.out.println(new Resolutionise(ls,10).lineal);
		System.out.println(new Resolutionise(ls,100).puntal);
		System.out.println(new Resolutionise(ls,1000).lineal);
		System.out.println("-------");
		ls = gf.createLineString(getCoordsArray(0, 0, 1000,509, 1000, 500, 0, 1));
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,10).lineal);
		System.out.println(new Resolutionise(ls,100).lineal);
		System.out.println("-------");
		ls = gf.createLineString(getCoordsArray(0, 1, 1000,1, 1000, 0, 1, 0, 1, -100, 0, -100, 0, 0));
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,1).lineal);
		System.out.println(new Resolutionise(ls,10).lineal);
		System.out.println(new Resolutionise(ls,100).lineal);

		System.out.println("-------");
		ls = gf.createLineString(getCoordsArray(-10,0, 0,0, 0,-10, 1,-10, 1,0, 10,0, 10,1, -10,1, -10, 0));
		System.out.println(ls);
		System.out.println(new Resolutionise(ls,10).lineal);
		System.out.println(new Resolutionise(ls,100).puntal);*/
	}

	public static Coordinate[] getCoordsArray(double ... data){
		Coordinate[] cs = new Coordinate[data.length/2];
		for(int i=0; i<data.length/2; i++) cs[i] = new Coordinate(data[i*2],data[i*2+1]);
		return cs;
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
