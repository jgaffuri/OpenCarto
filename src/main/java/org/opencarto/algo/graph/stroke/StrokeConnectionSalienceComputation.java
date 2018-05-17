/**
 * 
 */
package org.opencarto.algo.graph.stroke;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Node;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author julien Gaffuri
 *
 */
public class StrokeConnectionSalienceComputation {
	public final static Logger LOGGER = Logger.getLogger(StrokeConnectionSalienceComputation.class.getName());

	double computeSalience(Node n, Edge e1, Edge e2) {
		//compute deflation angle indicator
		double sal = getDeflationIndicator(n, e1, e2);
		//TODO compute salience also based on attributes of features
		Feature f1 = (Feature) e1.obj;
		Feature f2 = (Feature) e2.obj;
		return sal;
	};
	//between 0 (worst case) to 1 (perfect, no deflation)
	final double getDeflationIndicator(Node n, Edge e1, Edge e2) {
		Coordinate c = n.getC();
		Coordinate c1 = getCoordinateForDeflation(e1,n);
		Coordinate c2 = getCoordinateForDeflation(e2,n);
		double ang = Angle.angleBetween(c1, c, c2);
		//ang between 0 and Pi
		if(ang<0 || ang>Math.PI)
			LOGGER.warn("Unexpected deflection angle value around "+c+". Should be within [0,Pi]. "+ang);
		return ang / Math.PI;
	}

	final Coordinate getCoordinateForDeflation(Edge e, Node n) {
		Coordinate c = null;
		Coordinate[] cs = e.getCoords();
		if(n.getC().distance(cs[0]) == 0)
			c = cs[1];
		else if(n.getC().distance(cs[cs.length-1]) == 0)
			c = cs[cs.length-2];
		else
			LOGGER.warn("Could not getCoordinateForDeflation around "+n.getC());
		return c;
	}

}
