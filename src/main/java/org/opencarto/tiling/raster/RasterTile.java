/**
 * 
 */
package org.opencarto.tiling.raster;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.opencarto.datamodel.Feature;
import org.opencarto.tiling.Tile;

/**
 * @author julien Gaffuri
 *
 */
public class RasterTile<T extends Feature> extends Tile<T> {
	public int res = 256;
	public BufferedImage img;
	public Graphics2D g;

	public RasterTile(int x, int y, int z, Collection<T> fs){
		super(x, y, z, fs);
		img = new BufferedImage(res, res, BufferedImage.TYPE_INT_ARGB);
		g = (Graphics2D) img.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	public double toPixX(double xGeo){ return res*(xGeo-xMin)/(xMax-xMin); }
	public double toPixY(double yGeo){ return res*(1-(yGeo-yMin)/(yMax-yMin)); }

	public double getResX(){ return (xMax-xMin)/res; }
	public double getResY(){ return (yMax-yMin)/res; }

	@Override
	public void load(String imgFilepath) {
		File file = new File(imgFilepath + "." + RasterTileBuilder.format);

		//nothing to load
		if(!file.exists()) return;

		System.out.println("Incremental raster tiling not tested yet");

		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		g = (Graphics2D) img.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

}
