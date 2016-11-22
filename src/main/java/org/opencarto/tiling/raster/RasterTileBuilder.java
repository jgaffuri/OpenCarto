/**
 * 
 */
package org.opencarto.tiling.raster;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.opencarto.datamodel.Feature;
import org.opencarto.tiling.Tile;
import org.opencarto.tiling.TileBuilder;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author julien Gaffuri
 *
 */
public class RasterTileBuilder extends TileBuilder {
	protected String format = "png";
	protected Color color = Color.RED;
	protected int size = 10;

	public RasterTileBuilder(){}
	public RasterTileBuilder(Color color, int size){ this.color=color; this.size=size; }

	@Override
	public Tile createTile(int x, int y, int z, Collection<? extends Feature> fs) {
		return new RasterTile(x, y, z, fs);
	}

	@Override
	public void buildTile(Tile t_) {
		RasterTile t = (RasterTile)t_;
		t.g.setColor(color);

		//draw
		for (Feature ocf : t.fs) {
			Coordinate c = ocf.getGeom(t_.z).getCentroid().getCoordinate();
			t.g.fillOval((int)(t.toPixX(c.x)-size*0.5), (int)(t.toPixY(c.y)-size*0.5), size, size);
		}
	}

	@Override
	public void saveTile(Tile t, String folderPath, String fileName){
		new File(folderPath).mkdirs();
		try {
			ImageIO.write(((RasterTile)t).img, format, new File(folderPath + File.separator + fileName + "."+format));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
