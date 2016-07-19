/**
 * 
 */
package org.opencarto.tiling;

import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.datamodel.Feature;
import org.opencarto.util.ProjectionUtil;
import org.opencarto.util.TileUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Tile {
	//NB: the tile reference point is the top left corner.
	public int x,y,z;
	public ArrayList<Feature> fs;

	protected double xMin,xMax,yMin,yMax;
	public Envelope envelope;
	public LinearRing ring;
	public Polygon polygon;

	public Tile(int x, int y, int z, Collection<? extends Feature> fs_){
		this.x = x;
		this.y = y;
		this.z = z;
		this.xMin = ProjectionUtil.getXGeo( TileUtil.getLon(x, z) );
		this.xMax = ProjectionUtil.getXGeo( TileUtil.getLon(x+1, z) );
		this.yMin = ProjectionUtil.getYGeo( TileUtil.getLat(y+1, z) );
		this.yMax = ProjectionUtil.getYGeo( TileUtil.getLat(y, z) );
		envelope = new Envelope(xMin, xMax, yMin, yMax);
		ring = new GeometryFactory().createLinearRing(new Coordinate[]{new Coordinate(xMin,yMin),new Coordinate(xMax,yMin),new Coordinate(xMax,yMax),new Coordinate(xMin,yMax),new Coordinate(xMin,yMin)});
		polygon = new GeometryFactory().createPolygon(ring, null);

		//extract the features within the tile (x,y,z)
		this.fs = new ArrayList<Feature>();
		for (Feature f : fs_) {
			Geometry geom = f.getGeom(z);
			if(geom==null) continue;

			//check enveloppe and then geometry intersection
			if(!polygon.intersects(geom))
				continue;

			fs.add(f);
		}
	}

}
