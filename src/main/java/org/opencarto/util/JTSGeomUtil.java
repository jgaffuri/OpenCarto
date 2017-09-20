package org.opencarto.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

public class JTSGeomUtil {

	//return list of geometries that are not collections
	public static Collection<Geometry> getGeometries(Geometry geomIn){
		Collection<Geometry> geoms = new HashSet<Geometry>();
		if(!(geomIn instanceof GeometryCollection)){
			geoms.add(geomIn);
			return geoms;
		}
		GeometryCollection geomCol = (GeometryCollection) geomIn;
		for(int i=0; i<geomCol.getNumGeometries(); i++){
			Geometry geom = geomCol.getGeometryN(i);
			if(geom.isEmpty()) continue;
			if(geom instanceof GeometryCollection)
				geoms.addAll(getGeometries(geom));
			else geoms.add(geom);
		}
		return geoms;
	}

	//get multi form of a geometry
	public static Geometry toMulti(Geometry geom){
		if(geom instanceof GeometryCollection)
			return geom;
		else if(geom instanceof Point)
			return geom.getFactory().createMultiPoint(new Point[]{(Point)geom});
		else if(geom instanceof LineString)
			return geom.getFactory().createMultiLineString(new LineString[]{(LineString)geom});
		else if(geom instanceof Polygon)
			return geom.getFactory().createMultiPolygon(new Polygon[]{(Polygon)geom});
		System.err.println("Geom type not handeled: "+geom.getClass().getSimpleName());
		return null;
	}

	//intersection test for simple geometries
	public static boolean intersects(Geometry geom1, Geometry geom2){
		if(!(geom1 instanceof GeometryCollection) && !(geom2 instanceof GeometryCollection))
			return geom1.intersects(geom2);

		Collection<Geometry> geoms1 = getGeometries(geom1);
		Collection<Geometry> geoms2 = getGeometries(geom2);

		for(Geometry g1 : geoms1)
			for(Geometry g2 : geoms2)
				if(g1.intersects(g2))
					return true;
		return false;
	}

	//clean geometry
	public static Geometry clean(Geometry geom) {
		if(geom instanceof MultiPolygon || geom instanceof Polygon)
			return geom.buffer(0);
		if(geom instanceof MultiLineString){
			LineMerger lm = new LineMerger();
			lm.add(geom);
			@SuppressWarnings("unchecked")
			ArrayList<LineString> ml = (ArrayList<LineString>) lm.getMergedLineStrings();
			if(ml.size()==1) return (Geometry)ml.iterator().next();
			return geom.getFactory().createMultiLineString( (LineString[])ml.toArray(new LineString[ml.size()]) );
		}
		return geom;
	}

	//round geometry coordinates
	/*public static void round(Geometry geom, int decimalNb) {
		CoordinateSequence cs = null;
		if (geom instanceof Point) cs = ((Point)geom).getCoordinateSequence();
		else if (geom instanceof LineString) cs = ((LineString)geom).getCoordinateSequence();
		else if (geom instanceof Polygon) {
			Polygon poly = (Polygon)geom;
			round( poly.getExteriorRing(), decimalNb);
			for(int i=0; i<poly.getNumInteriorRing() ; i++)
				round( poly.getInteriorRingN(i), decimalNb);				
			return;
		}
		else if (geom instanceof GeometryCollection) {
			GeometryCollection gc = (GeometryCollection)geom;
			for(int i=0; i<gc.getNumGeometries(); i++)
				round(gc.getGeometryN(i), decimalNb);
			return;
		}
		else {
			System.err.println("JTS geometry type not treated: " + geom.getClass().getSimpleName());
			return;
		}

		//round the coordinates
		for(int i=0; i<cs.size(); i++) {
			Coordinate c = cs.getCoordinate(i);
			cs.setOrdinate(i, 0, Util.round(c.x, decimalNb));
			cs.setOrdinate(i, 1, Util.round(c.y, decimalNb));
			if(!Double.isNaN(c.z)) cs.setOrdinate(i, 2, Util.round(c.z, decimalNb));
		}
		geom.geometryChanged();
	}*/

	//keep only linear part of a geometry
	public static MultiLineString keepOnlyLinear(Geometry g) {
		final ArrayList<LineString> lss = new ArrayList<LineString>();
		g.apply(new GeometryComponentFilter() {
			public void filter(Geometry component) {
				if (component instanceof LineString)
					lss.add((LineString)component);
			}
		});
		if(lss.size()==0) return g.getFactory().createMultiLineString(new LineString[]{});
		return g.getFactory().createMultiLineString(lss.toArray(new LineString[lss.size()]));
	}

	//keep only polygonal part of a geometry
	public static MultiPolygon keepOnlyPolygonal(Geometry g) {
		final ArrayList<Polygon> mps = new ArrayList<Polygon>();
		g.apply(new GeometryComponentFilter() {
			public void filter(Geometry component) {
				if (component instanceof Polygon)
					mps.add((Polygon)component);
			}
		});
		if(mps.size()==0) return g.getFactory().createMultiPolygon(new Polygon[]{});
		return g.getFactory().createMultiPolygon(mps.toArray(new Polygon[mps.size()]));
	}

	//build polygon from envelope
	public static Polygon getGeometry(Envelope env) {
		Coordinate[] cs = new Coordinate[]{new Coordinate(env.getMinX(),env.getMinY()), new Coordinate(env.getMaxX(),env.getMinY()), new Coordinate(env.getMaxX(),env.getMaxY()), new Coordinate(env.getMinX(),env.getMaxY()), new Coordinate(env.getMinX(),env.getMinY())};
		return new GeometryFactory().createPolygon(cs);
	}




	//get all simple geometries
	public Collection<Geometry> getSimpleGeoms(Geometry geom){
		Collection<Geometry> out = new HashSet<Geometry>();
		if(geom.getNumGeometries()==0) return out;
		if(geom.getNumGeometries()==1)
			out.add(geom);
		else
			for(int i=0; i<geom.getNumGeometries(); i++)
				out.addAll(getSimpleGeoms(geom.getGeometryN(i)));
		return out;
	}

	//get all simple geometries
	public Collection<Geometry> getSimpleGeoms(Collection<Geometry> geoms){
		Collection<Geometry> out = new HashSet<Geometry>();
		for(Geometry geom : geoms) out.addAll(getSimpleGeoms(geom));
		return out;
	}


	public static Collection<Polygon> getPolygonGeometries(Geometry g) { return getPolygonGeometries(g, -1); }
	public static Collection<Polygon> getPolygonGeometries(Geometry g, double sizeDel) {
		Collection<Polygon> out = new ArrayList<Polygon>();
		for(Geometry g_ : getGeometries(g))
			if(g_ instanceof Polygon){
				if(sizeDel>0 && g_.getArea()<=sizeDel) continue;
				out.add((Polygon)g_);
			}
		return out ;
	}

}
