/**
 * 
 */
package org.opencarto.tiling.vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opencarto.MultiScaleFeature;
import org.opencarto.tiling.Tile;
import org.opencarto.tiling.TileBuilder;

import eu.europa.ec.eurostat.jgiscotools.feature.JTSGeomUtil;
import eu.europa.ec.eurostat.jgiscotools.util.ProjectionUtil;
import eu.europa.ec.eurostat.jgiscotools.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public class VectorTileBuilder<T extends MultiScaleFeature> extends TileBuilder<T> {
	protected static String format = "geojson";

	@Override
	public Tile<T> createTile(int x, int y, int z, Collection<T> fs) {
		return new VectorTile<T>(x, y, z, fs);
	}


	@Override
	public void load(Tile<T> t, String tileFilepath) {
		File file = new File(tileFilepath);

		//nothing to load
		if(!file.exists()) return;
		System.err.println("Incremental vector tiling not implemented yet");
	}


	@Override
	public void drawToTile(Tile<T> t_) {
		VectorTile<T> t = (VectorTile<T>)t_;

		//get the intersections
		for(MultiScaleFeature f : t.fs) {
			Collection<Geometry> geoms = JTSGeomUtil.getGeometries(f.getGeom(t_.z));
			ArrayList<Geometry> inters_ = new ArrayList<Geometry>();
			for(Geometry geom : geoms) {
				//compute intersection
				Geometry inter;
				try {
					inter = geom.intersection(t.polygon);
				} catch (Exception e) {
					//System.out.println("Warning: problem in intersection computation in tiling");
					continue;
				}

				if(inter==null){
					continue;
				} else if(inter.isEmpty()){
					continue;
				} else if(!inter.isValid()){
					System.err.println("Not valid inter");
					continue;
				}

				//compare geom and inter types
				if(geom instanceof Point){
					if( inter instanceof Point){
						inters_.add(inter);
					} else {
						System.out.println("Punctual intersection that is not punctual!!! : "+inter.getClass().getSimpleName());
						continue;
					}
				} else if(geom instanceof LineString){
					if( inter instanceof Point || inter instanceof MultiPoint ){
						continue;
					} else if( inter instanceof LineString || inter instanceof MultiLineString ){
						inters_.add(inter);
					} else {
						System.out.println("Linear intersection that is not linear!!! : "+inter.getClass().getSimpleName());
						continue;
					}
				} else if(geom instanceof Polygon){
					if( inter instanceof Point || inter instanceof MultiPoint || inter instanceof LineString || inter instanceof MultiLineString ){
						continue;
					} else if( inter instanceof Polygon || inter instanceof MultiPolygon ){
						inters_.add(inter);
					} else {
						System.out.println("Polygonal intersection that is not polygonal!!! : "+inter.getClass().getSimpleName());
						continue;
					}
				} else {
					System.out.println("Case to check: "+inter.getClass().getSimpleName());
					continue;
				}
			}

			if(inters_.size()==0)
				continue;

			Geometry interGeom = new GeometryFactory().createGeometryCollection(new Geometry[0]);
			for(Geometry geom : inters_)
				interGeom = interGeom.union(geom);

			//move to tile pix space
			geoToTilePix(interGeom, t.x, t.y, t.z);
			t.inters.put(f.getID(), interGeom);

			HashMap<String, Object> props = new HashMap<String, Object>();
			if(f.getAttributes()!=null) props.putAll(f.getAttributes());
			t.props.put(f.getID(), props);
		}
	}

	@Override
	public void saveTile(Tile<T> t_, String folderPath, String fileName) {
		VectorTile<T> t = (VectorTile<T>)t_;

		if(t.inters.size() == 0)
			return;

		//TODO
		System.err.println("Vector tile saving not implemented");
		
		/*boolean b = GeoJSONUtil.save(t.inters, t.props, folderPath, fileName+"."+format);
		if(!b) {
			System.out.println("Error in geojson file creation");
			return;
		}*/
		//t.props
		//GeoData.save(t.inters, folderPath+fileName+"."+format, crs);
	}


	public static void geoToTilePix(Geometry geom, int xTile, int yTile, int z){
		Coordinate[] cs = geom.getCoordinates();
		for(int i=0;i<cs.length;i++){
			Coordinate c=cs[i];
			//c.x = Util.round(ProjectionUtil.getXPixFromXGeo(c.x, z) - xTile*256 - 0.5, 1);
			//c.y = Util.round(ProjectionUtil.getYPixFromYGeo(c.y, z) - yTile*256 - 0.5, 1);
			c.x = (int)Util.round(ProjectionUtil.getXPixFromXGeo(c.x, z) - xTile*256 - 0.5, 0);
			c.y = (int)Util.round(ProjectionUtil.getYPixFromYGeo(c.y, z) - yTile*256 - 0.5, 0);
		}
	}

}
