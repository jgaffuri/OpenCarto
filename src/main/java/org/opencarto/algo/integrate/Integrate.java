/**
 * 
 */
package org.opencarto.algo.integrate;

import org.opencarto.algo.base.Copy;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.io.SHPUtil;
import org.opencarto.io.SHPUtil.SHPData;
import org.opencarto.processes.GeneralisationProcess;
import org.opencarto.util.Util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class Integrate {
	//TODO

	public static  Geometry perform(Geometry g, double distance) {
		Geometry out = Copy.perform(g);

		for(Coordinate c : out.getCoordinates()){
			c.x = Math.round(c.x/distance)*distance;
			c.y = Math.round(c.y/distance)*distance;
		}

		if(out instanceof Point) return out;
		else if(out instanceof MultiPoint) return out.union(out);
		else if(out instanceof LineString) return out.union(out);
		else if(out instanceof MultiLineString) return out.union(out);
		else if(out instanceof Polygon) return out.buffer(0);
		else if(out instanceof MultiPolygon) return out.buffer(0);
		return out;
	}



	public static void main(String[] args) {
		String inPath = "/home/juju/Bureau/repoju/data/fish/CNTR_2010_03M_SH/cntr_wm.shp";
		SHPData data = SHPUtil.loadSHP(inPath);
		System.out.println(data.fs.size() + " countries loaded.");
		int zoomMax = 7;
		ZoomExtend zs = new ZoomExtend(0,zoomMax);

		for(int z=zs.max; z>=zs.min; z--){
			//get resolution value
			double res = GeneralisationProcess.getResolution(z);
			System.out.println("Generalisation: "+z+" (resolution "+Util.round(res, 1)+"m)");

			//make individual generalisation
			for(Feature f: data.fs){
				Geometry geom = f.getGeom();
				if(geom==null) continue;
				f.setGeom( Integrate.perform(geom, res) );
			}

			SHPUtil.saveSHP(data.fs, "/home/juju/Bureau/repoju/data/fish/CNTR_2010_03M_SH/", "cntr_"+z+".shp");
		}

		System.out.println("Done.");
	}

}
