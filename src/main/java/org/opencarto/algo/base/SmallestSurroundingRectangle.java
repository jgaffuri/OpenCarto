package org.opencarto.algo.base;

import org.apache.log4j.Logger;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;

/**
 * 
 * @author julien Gaffuri
 *
 */
public class SmallestSurroundingRectangle {
	public static Logger logger = Logger.getLogger(SmallestSurroundingRectangle.class.getName());

	public static Polygon get(Geometry geom){
		Geometry hull_ = (new ConvexHull(geom)).getConvexHull();
		if (!(hull_ instanceof Polygon)) return null;
		Polygon convHull = (Polygon)hull_;

		Coordinate c = geom.getCentroid().getCoordinate();
		Coordinate[] coords = convHull.getExteriorRing().getCoordinates();

		double minArea = Double.MAX_VALUE, minAngle = 0.0;
		Polygon ssr = null;
		Coordinate ci = coords[0], cii;
		for(int i=0; i<coords.length-1; i++){
			cii = coords[i+1];
			double angle = Math.atan2(cii.y-ci.y, cii.x-ci.x);
			//Polygon rect = (Polygon) Rotation.get(convHull, c, -1.0*angle).getEnvelope();
			Polygon rect = (Polygon) AffineTransformation.rotationInstance(-1.0*angle, c.getX(), c.getY()).transform(convHull);
			double area = rect.getArea();
			if (area < minArea) {
				minArea = area;
				ssr = rect;
				minAngle = angle;
			}
			ci = cii;
		}
		//return Rotation.get(ssr, c, minAngle);
		return (Polygon) AffineTransformation.rotationInstance(minAngle, c.getX(), c.getY()).transform(ssr);
	}

	public static Polygon get(Geometry geom, boolean preserveSize){
		if( !preserveSize ) return get(geom);

		Polygon out = get(geom);
		double ini = geom.getArea();
		double fin = out.getArea();

		if(fin == 0) {
			logger.warn("Failed to preserve size of smallest surrounding rectangle: Null final area.");
			return out;
		}

		double k = Math.sqrt(ini/fin);
		Coordinate c = out.getCentroid().getCoordinate();
		out = (Polygon) AffineTransformation.scaleInstance(k,k, c.x,c.y).transform(out);
		return out;
		//return Scaling.get(out, out.getCentroid().getCoordinate(), Math.sqrt(ini/fin));
	}

	
	public static void main(String[] args) {
		
		//TODO replace with:
		//MinimumDiameter.getMinimumRectangle();
		
		
	}
	
}
