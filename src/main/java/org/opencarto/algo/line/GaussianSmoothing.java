package org.opencarto.algo.line;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

/**
 * Apply a gaussian smoothing to a line.
 * 
 * @author julien Gaffuri
 *
 */
public class GaussianSmoothing {
	public static final Logger LOGGER = Logger.getLogger(GaussianSmoothing.class.getName());

	//TODO adopt GeometryTransformer
	//TODO write tests
	//TODO pull to JTS with densifier

	public static LineString get(LineString ls, double sigmaM){ return get(ls, sigmaM, -1); }
	public static LineString get(LineString ls, double sigmaM, double resolution){
		if(ls.getCoordinates().length <= 2) return (LineString) ls.copy();

		boolean isClosed = ls.isClosed();
		double length = ls.getLength();
		double densifiedResolution = sigmaM/3;

		//handle extreme cases
		//too large sigma resulting in too large densified resolution
		if(densifiedResolution > 0.25*length ) {
			if(isClosed){
				//return tiny triangle
				//TODO do better? return center point?
				return ls.getFactory().createLineString(new Coordinate[]{ ls.getCoordinateN(0), ls.getCoordinateN(1), ls.getCoordinateN(ls.getNumPoints()-2), ls.getCoordinateN(ls.getNumPoints()-1) });
			} else {
				//return segment
				return ls.getFactory().createLineString(new Coordinate[]{ ls.getCoordinateN(0), ls.getCoordinateN(ls.getNumPoints()-1) });
			}
		}

		//compute densified line
		Coordinate[] densifiedCoords = LittleThumblingDensifier.densify(ls, densifiedResolution).getCoordinates();

		//build ouput line structure
		int nb = (int) (length/densifiedResolution);
		Coordinate[] out = new Coordinate[nb+1];

		//prepare gaussian coefficients
		int n = 7*3; //it should be E(7*sigma/densifiedResolution) which is 7*3;
		double gcs[] = new double[n+1];
		{
			double a = sigmaM*Math.sqrt(2*Math.PI);
			double b = sigmaM*sigmaM*2;
			double d = densifiedResolution*densifiedResolution;
			for(int i=0; i<n+1; i++) gcs[i] = Math.exp(-i*i*d/b) /a;
		}

		Coordinate c0 = densifiedCoords[0];
		Coordinate cN = densifiedCoords[nb];
		for(int i=0; i<nb; i++) {
			if(!isClosed && i==0) continue;

			//compute coordinates of point i of the smoothed line (gauss mean)
			double x=0.0, y=0.0;
			for(int j=-n; j<=n; j++) {
				//index of the point to consider on the original densified line
				int q = i+j;
				//find coordinates (xq,yq) of point q
				double xq, yq;
				if(q<0) {
					if(isClosed) {
						q = q%nb; if(q<0) q+=nb;
						Coordinate c = densifiedCoords[q];
						xq = c.x;
						yq = c.y;
					} else {
						//get symetric point
						q = (-q)%nb; if(q==0) q=nb;
						Coordinate c = densifiedCoords[q];
						xq = 2*c0.x-c.x;
						yq = 2*c0.y-c.y;
					}
				} else if (q>nb) {
					if(isClosed) {
						q = q%nb; if(q==0) q=nb;
						Coordinate c = densifiedCoords[q];
						xq = c.x;
						yq = c.y;
					} else {
						//get symetric point
						q = nb-q%nb; if(q==nb) q=0;
						Coordinate c = densifiedCoords[q];
						xq = 2*cN.x-c.x;
						yq = 2*cN.y-c.y;
					}
				} else {
					//general case (most frequent)
					Coordinate c = densifiedCoords[q];
					xq = c.x;
					yq = c.y;
				}
				//get gaussian coefficient
				double gc = gcs[j>=0?j:-j];
				//add contribution of point q to new position of point i
				x += xq*gc;
				y += yq*gc;
			}
			out[i] = new Coordinate(x*densifiedResolution, y*densifiedResolution);
		}

		//handle start and end points
		if(isClosed) {
			out[nb] = out[0];
		} else {
			out[0] = densifiedCoords[0];
			out[nb] = densifiedCoords[densifiedCoords.length-1];
		}

		LineString lsOut = ls.getFactory().createLineString(out);
		if(resolution<0) resolution = densifiedResolution /3;
		lsOut = (LineString) DouglasPeuckerRamerFilter.get( lsOut , resolution);

		return lsOut;
	}

}
