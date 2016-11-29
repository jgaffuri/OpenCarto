/**
 * 
 */
package org.opencarto.tiling.vector;

import java.util.Collection;
import java.util.HashMap;

import org.opencarto.datamodel.Feature;
import org.opencarto.tiling.Tile;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class VectorTile<T extends Feature> extends Tile<T> {
	//map with object id -> intersecting geometry
	public HashMap<String,Geometry> inters = new HashMap<String,Geometry>();
	public HashMap<String,HashMap<String, Object>> props = new HashMap<String,HashMap<String, Object>>();

	public VectorTile(int x, int y, int z, Collection<T> fs){
		super(x, y, z, fs);
	}

}
