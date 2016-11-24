/**
 * 
 */
package org.opencarto.style;

import java.awt.Graphics2D;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A cartographic style.
 * 
 * @author julien Gaffuri
 *
 */
public abstract class Style {

	/**
	 *
	 * The drawing method. This method describes how the object is drawn according to the style.
	 * 
	 * @param geom The geometry to draw.
	 * @param pt The object to convert geometries into drawable shapes.
	 * @param gr The canvas to draw on.
	 */
	public abstract void draw(Geometry geom, PointTransformation pt, Graphics2D gr);

	/**
	 * @param geom The geometry to draw.
	 * @return The footprint geometry of the object symbol.
	 */
	//public abstract Geometry footprint(Geometry st);

	private double xOffset=0;
	public double getxOffset() { return this.xOffset; }
	public void setxOffset(double xOffset) { this.xOffset = xOffset; }
	private double yOffset=0;
	public double getyOffset() { return this.yOffset; }
	public void setyOffset(double yOffset) { this.yOffset = yOffset; }
}
