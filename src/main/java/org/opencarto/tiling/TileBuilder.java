/**
 * 
 */
package org.opencarto.tiling;

import java.util.Collection;

import org.opencarto.datamodel.MultiScaleFeature;

/**
 * @author julien Gaffuri
 *
 */
public abstract class TileBuilder<T extends MultiScaleFeature> {
	public abstract Tile<T> createTile(int x, int y, int z, Collection<T> fs);
	public abstract void load(Tile<T> t, String tileFilepath);
	public abstract void drawToTile(Tile<T> t);
	public abstract void saveTile(Tile<T> t, String folderPath, String fileName);
}
