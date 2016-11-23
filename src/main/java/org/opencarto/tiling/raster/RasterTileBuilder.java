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
import com.vividsolutions.jts.geom.Geometry;

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
			Geometry geom = ocf.getGeom(t_.z);
			String gt = geom.getGeometryType();
			if("POINT".equals(gt)){
				Coordinate c = geom.getCoordinate();
				t.g.fillOval((int)(t.toPixX(c.x)-size*0.5), (int)(t.toPixY(c.y)-size*0.5), size, size);
				//} else if("LINESTRING".equals(gt)){
				//See https://sourceforge.net/p/opencarto/code/1185/tree/trunk/client/opencarto-applet/src/main/java/org/opencarto/style/
				//See https://sourceforge.net/p/opencarto/code/1185/tree/trunk/client/opencarto-base/src/main/java/org/opencarto/util/
				//import com.vividsolutions.jts.awt.ShapeWriter; - gr.draw( sw.toShape(geom) );
				//TODO
			} else {
				//TODO
				Coordinate c = geom.getCentroid().getCoordinate();
				t.g.fillOval((int)(t.toPixX(c.x)-size*0.5), (int)(t.toPixY(c.y)-size*0.5), size, size);
			}
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
