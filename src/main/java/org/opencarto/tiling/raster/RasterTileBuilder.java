/**
 * 
 */
package org.opencarto.tiling.raster;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.locationtech.jts.geom.Coordinate;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.MultiScaleProperty;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.style.basic.BasicStyle;
import org.opencarto.tiling.Tile;
import org.opencarto.tiling.TileBuilder;

/**
 * @author julien Gaffuri
 *
 */
public class RasterTileBuilder<T extends Feature> extends TileBuilder<T> {
	protected static String format = "png";
	protected MultiScaleProperty<Style<T>> style = new MultiScaleProperty<Style<T>>(new BasicStyle<T>());

	public RasterTileBuilder(){}
	public RasterTileBuilder(MultiScaleProperty<Style<T>> style){ this.style=style; }

	@Override
	public Tile<T> createTile(int x, int y, int z, Collection<T> fs) {
		return new RasterTile<T>(x, y, z, fs);
	}

	@Override
	public void load(Tile<T> t_, String imgFilepath) {
		RasterTile<T> t = (RasterTile<T>)t_;

		//tile file
		File file = new File(imgFilepath + "." + format);

		//no file exist: nothing to load
		if(!file.exists()) return;

		try {
			t.img = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		t.g = (Graphics2D) t.img.getGraphics();
		t.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	@Override
	public void drawToTile(Tile<T> tile_) {
		final RasterTile<T> tile = (RasterTile<T>)tile_;

		//build point transformation
		PointTransformation pt = new PointTransformation(){
			public Point2D geoToPix(Coordinate c) { return new Point2D.Double(tile.toPixX(c.x),tile.toPixY(c.y)); }
		};

		//draw
		for (T f : tile.fs){
			int z = tile.z;

			//select style, in priority the feature style at scale z, otherwise the default ones.
			Style<T> style_;
			if(f.getStyle(z)!=null) style_ = f.getStyle(z);
			else if(f.getStyle()!=null) style_ = f.getStyle();
			else if(style.get(z)!=null) style_ = style.get(z);
			else style_ = style.get();

			//draw the feature on the tile with the selected style
			style_.draw(f ,z, pt, tile.g);
		}
	}

	@Override
	public void saveTile(Tile<T> t, String folderPath, String fileName){
		new File(folderPath).mkdirs();
		try {
			ImageIO.write(((RasterTile<T>)t).img, format, new File(folderPath + File.separator + fileName + "."+format));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
