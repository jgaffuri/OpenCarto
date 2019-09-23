/**
 * 
 */
package org.opencarto.tiling;

import java.util.ArrayList;
import java.util.Collection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.opencarto.datamodel.MultiScaleFeature;
import org.opencarto.util.ProjectionUtil;
import org.opencarto.util.TileUtil;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Tile<T extends MultiScaleFeature> {
	//NB: the tile reference point is the top left corner.
	public int x,y,z;
	public ArrayList<T> fs;

	//NB: projection is web mercator
	protected double xMin,xMax,yMin,yMax;
	public Polygon polygon;

	public Tile(int x, int y, int z, Collection<T> fs_){
		this.x = x; this.y = y; this.z = z;
		this.xMin = ProjectionUtil.getXGeo( TileUtil.getLon(x, z) );
		this.xMax = ProjectionUtil.getXGeo( TileUtil.getLon(x+1, z) );
		this.yMin = ProjectionUtil.getYGeo( TileUtil.getLat(y+1, z) );
		this.yMax = ProjectionUtil.getYGeo( TileUtil.getLat(y, z) );
		//envelope = new Envelope(xMin, xMax, yMin, yMax);
		LinearRing ring = new GeometryFactory().createLinearRing(new Coordinate[]{new Coordinate(xMin,yMin),new Coordinate(xMax,yMin),new Coordinate(xMax,yMax),new Coordinate(xMin,yMax),new Coordinate(xMin,yMin)});
		polygon = new GeometryFactory().createPolygon(ring, null);

		//retrieve the features within the tile (x,y,z)
		//TODO use spatial index?
		this.fs = new ArrayList<T>();
		for (T f : fs_) {
			Geometry geom = f.getGeom(z);
			if(geom==null) continue;

			//check enveloppe and then geometry intersection
			if(polygon.intersects(geom)) fs.add(f);
		}
	}

}
