/**
 * 
 */
package org.opencarto.tiling;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;

/**
 * @author julien Gaffuri
 *
 */
public class Tiling<T extends Feature> {

	private Collection<T> fs;
	private ZoomExtend zs;
	private TileBuilder<T> tb;
	private String outputFolder;
	private ArrayList<int[]> report;
	private boolean withReport = false;

	public Tiling(Collection<T> fs, TileBuilder<T> tb, String outputFolder, ZoomExtend zs, boolean withReport){
		this.fs = fs;
		this.zs = zs;
		this.tb = tb;
		this.outputFolder = outputFolder;
		this.report = new ArrayList<int[]>();
		this.withReport = withReport;
	}

	public void doTiling(){ doTiling(false); }
	public void doTiling(boolean incremental){
		if(fs == null || fs.size() == 0)
			return;
		doTiling(0, 0, 0, zs.min, zs.max, incremental);

		//export report
		if(withReport) exportReport();
	}

	private void doTiling(int x, int y, int z, int zMin, int zMax, boolean incremental){
		if(zMax < z) return; //too deep: return
		if(zMin <= z){
			//get tile
			Tile<T> t = tb.createTile(x, y, z, fs);

			String folderPath = outputFolder+File.separator+"g"+File.separator+z+File.separator+x+File.separator;

			if(incremental)
				t.load(folderPath + y);

			//empty tile: stop
			if(t.fs.size() == 0)
				return;

			//build and save the tile
			tb.buildTile(t);
			tb.saveTile(t, folderPath, y+"");

			//report
			if(withReport) report.add(new int[]{x,y,z});
		}

		//launch sub level tiling
		doTiling(2*x  , 2*y  ,z+1, zMin, zMax, incremental);
		doTiling(2*x+1, 2*y  ,z+1, zMin, zMax, incremental);
		doTiling(2*x  , 2*y+1,z+1, zMin, zMax, incremental);
		doTiling(2*x+1, 2*y+1,z+1, zMin, zMax, incremental);
	}

	private void exportReport() {
		System.out.println("--- Tiling report ---");
		System.out.println(report.size() + " tiles built.");

		int zNb = zs.size();

		//get image size
		int res = (int)Math.pow(2, zNb-1);
		//System.out.println("("+res+" pix)");
		//prepare image
		BufferedImage img = new BufferedImage(res, res, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, res, res);

		//initialise number of tiles and color per zoom level
		int[] zCount = new int[zNb];
		Color[] cols = new Color[zNb];
		for(int z=0; z<zNb; z++){
			zCount[z]=0;
			int t=255*(z+1)/zNb;
			cols[z] = new Color(t,t,t);
		}

		//go through tiles
		for(int[] t : report){
			int x=t[0], y=t[1];
			int z=t[2]-zs.min;
			int w = (int) (res/Math.pow(2,z));
			g.setColor(cols[z]);
			g.fillRect(x*w, y*w, w, w);
			zCount[z]++;
		}

		//print report
		for(int i=0; i<zCount.length; i++){
			System.out.println("   z=" + (i+zs.min) + " -> " + zCount[i] + " tiles.");
		}

		//save image
		try {
			File f = new File(outputFolder + File.separator + "report.png");
			f.mkdirs();
			ImageIO.write(img, "png", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("--- End of tiling report ---");
	}

}
