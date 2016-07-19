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
public abstract class TileBuilder {
	public abstract Tile createTile(int x, int y, int z, Collection<? extends Feature> fs);
	public abstract void buildTile(Tile t);
	public abstract void saveTile(Tile t, String folderPath, String fileName);
}
