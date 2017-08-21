package org.opencarto.algo.line;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencarto.algo.base.DouglasPeuckerRamerFilter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class GaussianSmoothing {
	private static Logger logger = Logger.getLogger(GaussianSmoothing.class.getName());

	public static LineString get(LineString ls, double sigma, double resolution){
		if(ls.getCoordinates().length <= 2) return ls;
		if(ls.isClosed()) {
			logger.log(Level.WARNING, "Closed line not supported yet in gaussian smoothing");
			return ls;
		}

		//compute densified line
		double densifiedResolution = resolution>0? resolution*0.25 : 1.0;
		Coordinate[] densifiedCoordinates = LineDensification.get(ls, densifiedResolution).getCoordinates();

		//build ouput line structure
		int nb=(int) (ls.getLength()/densifiedResolution);
		Coordinate[] out = new Coordinate[nb+1];

		//prepare gaussian coefficients
		int n=7*(int)sigma;
		double gc[] = new double[n+1];
		{
			double a = sigma*Math.sqrt(2*Math.PI);
			double b = sigma*sigma*2;
			for(int i=0; i<n+1; i++) gc[i]=Math.exp(-i*i/b)/a;
		}

		int q;
		Coordinate c;
		double x,y,dx,dy;
		Coordinate c0 = densifiedCoordinates[0];
		Coordinate cN = densifiedCoordinates[nb];
		for(int i=0; i<nb; i++) {

			//point i of the smoothed line (gauss mean)
			x=0.0; y=0.0;
			for(int j=-n; j<=n; j++) {
				q=i+j;
				if(q<0) {
					c=densifiedCoordinates[-q];
					//symetric of initial point
					dx=2*c0.x-c.x;
					dy=2*c0.y-c.y;
				}
				else if (i+j>nb) {
					c=densifiedCoordinates[2*nb-q];
					//symetric of final point
					dx=2*cN.x-c.x;
					dy=2*cN.y-c.y;
				}
				else {
					c = densifiedCoordinates[q];
					dx = c.x;
					dy = c.y;
				}
				x += dx*gc[j>=0?j:-j];
				y += dy*gc[j>=0?j:-j];
			}
			out[i] = new Coordinate(x, y);
		}
		out[nb]= densifiedCoordinates[densifiedCoordinates.length-1];
		if(resolution > 0) {
			return (LineString) DouglasPeuckerRamerFilter.get( new GeometryFactory().createLineString(out) , resolution);
		}
		return new GeometryFactory().createLineString(out);
	}
}
