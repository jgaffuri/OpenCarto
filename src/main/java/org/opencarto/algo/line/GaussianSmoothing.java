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

	//TODO handle closed line
	//TODO follow JTS line filter schema, like DP?

	public static LineString get(LineString ls, double sigmaM){ return get(ls, sigmaM, -1); }
	public static LineString get(LineString ls, double sigmaM, double resolution){
		if(ls.getCoordinates().length <= 2) return ls;

		boolean isClosed = ls.isClosed();
		double length = ls.getLength();
		double densifiedResolution = sigmaM/3;

		//handle extreme cases
		//too large sigma resulting in too large densified resolution
		if(densifiedResolution > 0.25*length ) {
			if(isClosed){
				//return clone. return a triangle instead?
				return ls.getFactory().createLineString(ls.getCoordinates());
			} else {
				//return segment
				return ls.getFactory().createLineString(new Coordinate[]{ ls.getCoordinateN(0), ls.getCoordinateN(ls.getNumPoints()-1) });
			}
		}

		if(isClosed) {
			LOGGER.warn("Closed line not supported yet in gaussian smoothing");
			//TODO handle this case
			return ls;
		}

		//compute densified line
		Coordinate[] densifiedCoords = DensifierStep.densify(ls, densifiedResolution).getCoordinates();

		//build ouput line structure
		int nb = (int) (length/densifiedResolution);
		Coordinate[] out = new Coordinate[nb+1];

		//prepare gaussian coefficients
		int n = 7*3; //it should be E(7*sigma/densifiedResolution) which is 7*3;
		double gc[] = new double[n+1];
		{
			double a = sigmaM*Math.sqrt(2*Math.PI);
			double b = sigmaM*sigmaM*2;
			double d = densifiedResolution*densifiedResolution;
			for(int i=0; i<n+1; i++) gc[i] = Math.exp(-i*i*d/b) /a;
		}

		int q=0;
		Coordinate c0 = densifiedCoords[0];
		Coordinate cN = densifiedCoords[nb];
		for(int i=1; i<nb; i++) {

			//point i of the smoothed line (gauss mean)
			double x=0.0, y=0.0;
			for(int j=-n; j<=n; j++) {
				//try {
				q = i+j;
				//add contribution (dx,dy) of point q
				double dx, dy; 
				if(q<0) {
					int q2=-q;
					while(q2>nb) q2-=nb;
					Coordinate c = densifiedCoords[q2];
					//symetric of initial point
					dx = 2*c0.x-c.x;
					dy = 2*c0.y-c.y;
				} else if (q>nb) {
					int q2=q=2*nb-q;
					while(q2<0) q2+=nb;
					Coordinate c = densifiedCoords[q2];
					//symetric of final point
					dx = 2*cN.x-c.x;
					dy = 2*cN.y-c.y;
				} else {
					Coordinate c = densifiedCoords[q];
					dx = c.x;
					dy = c.y;
				}
				double g = gc[j>=0?j:-j];
				x += dx*g;
				y += dy*g;
				/*} catch (Exception e) {
					//System.out.println("-----");
					//System.out.println(e.getMessage());
					//System.out.println("nb_pts="+ls.getNumPoints()+"   length="+length);
					//System.out.println("nb_fin="+nb+"   sigmaM="+sigmaM);
					//System.out.println("i="+i+"   j="+j+"   q="+q);
					//e.printStackTrace();
					throw e;
				}*/
			}
			out[i] = new Coordinate(x*densifiedResolution, y*densifiedResolution);
		}
		out[0] = densifiedCoords[0];
		out[nb] = densifiedCoords[densifiedCoords.length-1];

		LineString lsOut = ls.getFactory().createLineString(out);
		if(resolution<0) resolution = densifiedResolution /3;
		lsOut = (LineString) DouglasPeuckerRamerFilter.get( lsOut , resolution);

		//if(lsOut.getCoordinateN(0).distance(ls.getCoordinateN(0))>0) System.err.println("Pb0");
		//if(lsOut.getCoordinateN(lsOut.getNumPoints()-1).distance(ls.getCoordinateN(ls.getNumPoints()-1))>0) System.err.println("PbN");

		return lsOut;
	}

}
