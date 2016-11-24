/**
 * 
 */
package org.opencarto.style;

import java.awt.geom.Point2D;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author julien Gaffuri
 *
 */
public interface PointTransformation {

	public Point2D geoToPix(Coordinate c);

}
