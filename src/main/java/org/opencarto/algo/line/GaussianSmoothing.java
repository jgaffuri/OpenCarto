package org.opencarto.algo.line;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencarto.algo.base.DouglasPeuckerRamerFilter;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class GaussianSmoothing {
	public static Logger logger = Logger.getLogger(GaussianSmoothing.class.getName());

	public static LineString get(LineString ls, double sigmaM){ return get(ls, sigmaM, -1); }
	public static LineString get(LineString ls, double sigmaM, double resolution){
		if(ls.getCoordinates().length <= 2) return ls;

		boolean closed = ls.isClosed();
		if(closed) {
			logger.log(Level.WARNING, "Closed line not supported yet in gaussian smoothing");
			return ls;
		}

		//compute densified line
		double densifiedResolution = sigmaM/3;
		Coordinate[] densifiedCoords = LineDensification.get(ls, densifiedResolution).getCoordinates();

		//build ouput line structure
		int nb = (int) (ls.getLength()/densifiedResolution);
		Coordinate[] out = new Coordinate[nb+1];

		//prepare gaussian coefficients
		int n = 3*7; //it should be E(7*sigma/densifiedResolution) which is 7*3;
		double gc[] = new double[n+1];
		double sg=0;
		{
			double a = sigmaM*Math.sqrt(2*Math.PI);
			double b = sigmaM*sigmaM*2;
			double d = densifiedResolution*densifiedResolution;
			for(int i=0; i<n+1; i++) gc[i] = Math.exp(-i*i*d/b) /a;
			for(int i=1; i<n+1; i++) sg+=gc[i];
			sg+=sg+gc[0];
		}

		int q;
		Coordinate c;
		double x,y,dx,dy,g;
		Coordinate c0 = densifiedCoords[0];
		Coordinate cN = densifiedCoords[nb];
		for(int i=0; i<nb; i++) {

			//point i of the smoothed line (gauss mean)
			x=0.0; y=0.0;
			for(int j=-n; j<=n; j++) {
				q = i+j;
				//q = q%nb; //use that?
				//add contribution (dx,dy) of point q
				if(q<0) {
					if(!closed){
						c = densifiedCoords[-q];
						//symetric of initial point
						dx = 2*c0.x-c.x;
						dy = 2*c0.y-c.y;
					} else {
						//TODO check that
						c = densifiedCoords[q+nb];
						dx = c.x;
						dy = c.y;
					}
				} else if (q>nb) {
					if(!closed){
						c = densifiedCoords[2*nb-q];
						//symetric of final point
						dx = 2*cN.x-c.x;
						dy = 2*cN.y-c.y;
					} else {
						//TODO check that
						c = densifiedCoords[q-nb];
						dx = c.x;
						dy = c.y;
					}
				} else {
					c = densifiedCoords[q];
					dx = c.x;
					dy = c.y;
				}
				g = gc[j>=0?j:-j];
				x += dx*g;
				y += dy*g;
			}
			out[i] = new Coordinate(x/sg, y/sg);
		}
		out[nb]= densifiedCoords[densifiedCoords.length-1];

		LineString lsOut = ls.getFactory().createLineString(out);
		if(resolution<0) resolution = densifiedResolution /3;
		lsOut = (LineString) DouglasPeuckerRamerFilter.get( lsOut , resolution);
		return lsOut;
	}




	public static void main(String[] args) {
		ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/nuts_gene_data/nuts_2013/1M/LAEA/lvl3/BN.shp", 3035).fs;
		for(Feature f : fs){
			LineString ls = (LineString) JTSGeomUtil.getGeometries(f.getGeom()).iterator().next();
			if(ls.isClosed()) continue;
			System.out.println(f.id);
			try {
				f.setGeom( GaussianSmoothing.get(ls, 1000, -1000) );
			} catch (Exception e) {
				System.err.println("Failed!");
			}
		}
		SHPUtil.saveSHP(fs, "/home/juju/Bureau/", "gauss.shp");
		System.out.println("End");
	}

}
