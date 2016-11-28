/**
 * 
 */
package org.opencarto.tiling.raster;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.MultiScaleProperty;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.style.basic.BasicStyle;
import org.opencarto.tiling.Tile;
import org.opencarto.tiling.TileBuilder;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author julien Gaffuri
 *
 */
public class RasterTileBuilder extends TileBuilder {
	protected String format = "png";
	protected MultiScaleProperty<Style<Feature>> style = new MultiScaleProperty<Style<Feature>>(new BasicStyle());

	public RasterTileBuilder(){}
	public RasterTileBuilder(MultiScaleProperty<Style<Feature>> style){ this.style=style; }

	@Override
	public Tile createTile(int x, int y, int z, Collection<? extends Feature> fs) {
		return new RasterTile(x, y, z, fs);
	}

	@Override
	public void buildTile(Tile t_) {
		final RasterTile t = (RasterTile)t_;

		//build point transformation
		PointTransformation pt = new PointTransformation(){
			public Point2D geoToPix(Coordinate c) { return new Point2D.Double(t.toPixX(c.x),t.toPixY(c.y)); }
		};

		//draw
		for (Feature f : t.fs)
			style.get(t_.z).draw(f ,t_.z, pt, t.g);
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
