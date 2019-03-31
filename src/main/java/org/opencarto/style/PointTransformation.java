/**
 * 
 */
package org.opencarto.style;

import java.awt.geom.Point2D;

import org.locationtech.jts.geom.Coordinate;

/**
 * @author julien Gaffuri
 *
 */
public interface PointTransformation {

	public Point2D geoToPix(Coordinate c);

}
