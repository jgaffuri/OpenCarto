/**
 * 
 */
package org.opencarto.tiling;

import java.util.Collection;

import org.opencarto.datamodel.Feature;

/**
 * @author julien Gaffuri
 *
 */
public abstract class TileBuilder<T extends Feature> {
	public abstract Tile<T> createTile(int x, int y, int z, Collection<T> fs);
	public abstract void buildTile(Tile<T> t);
	public abstract void saveTile(Tile<T> t, String folderPath, String fileName);
}
