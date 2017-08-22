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

	public static LineString get(LineString ls, double sigmaM, double resolution){
		if(ls.getCoordinates().length <= 2) return ls;

		boolean closed = ls.isClosed();
		if(closed) {
			logger.log(Level.WARNING, "Closed line not supported yet in gaussian smoothing");
			return ls;
		}

		//compute densified line
		double densifiedResolution = resolution>0? resolution*0.25 : 1.0; //TODO should depend on sigma as well?
		Coordinate[] densifiedCoords = LineDensification.get(ls, densifiedResolution).getCoordinates();

		//build ouput line structure
		int nb = (int) (ls.getLength()/densifiedResolution);
		Coordinate[] out = new Coordinate[nb+1];

		/*
		System.out.println("resolution = "+resolution);
		System.out.println("sigma = "+sigma);
		System.out.println("Densified resolution = "+densifiedResolution);
		System.out.println("densifiedCoordinates.length = "+densifiedCoords.length);
		System.out.println("ls.getLength() = "+ls.getLength());
		System.out.println("(int) (ls.getLength()/densifiedResolution) = "+nb);
		System.out.println("out.length = "+out.length);*/

		//prepare gaussian coefficients
		int n = (int)( 6*sigmaM/densifiedResolution ) +1;
		double gc[] = new double[n+1];
		{
			double a = sigmaM*Math.sqrt(2*Math.PI);
			double b = sigmaM*sigmaM*2;
			double d = densifiedResolution*densifiedResolution;
			for(int i=0; i<n+1; i++) gc[i] = Math.exp(-i*i*d/b) /a;
		}

		int q;
		Coordinate c;
		double x,y,dx,dy;
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
				x += dx*gc[j>=0?j:-j];
				y += dy*gc[j>=0?j:-j];
			}
			out[i] = new Coordinate(x, y);
		}
		out[nb]= densifiedCoords[densifiedCoords.length-1];

		LineString lsOut = ls.getFactory().createLineString(out);
		if(resolution > 0) lsOut = (LineString) DouglasPeuckerRamerFilter.get( lsOut , resolution);
		return lsOut;
	}




	public static void main(String[] args) {
		ArrayList<Feature> fs = SHPUtil.loadSHP("/home/juju/Bureau/nuts_gene_data/nuts_2013/1M/LAEA/lvl3/BN.shp", 3035).fs;
		for(Feature f : fs){
			LineString ls = (LineString) JTSGeomUtil.getGeometries(f.getGeom()).iterator().next();
			if(ls.isClosed()) continue;
			System.out.println(f.id);
			try {
				f.setGeom( GaussianSmoothing.get(ls, 1000, 1000) );
			} catch (Exception e) {
				System.err.println("Failed!");
			}
		}
		SHPUtil.saveSHP(fs, "/home/juju/Bureau/", "gauss.shp");
	}

}
