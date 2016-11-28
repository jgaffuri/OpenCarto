/**
 * 
 */
package org.opencarto.style;

import java.awt.Graphics2D;

import org.opencarto.datamodel.Feature;

/**
 * A cartographic style.
 * 
 * @author julien Gaffuri
 *
 */
public abstract class Style<T extends Feature> {

	/**
	 *
	 * The drawing method. This method describes how the object is drawn according to the style.
	 * 
	 * @param geom The geometry to draw.
	 * @param pt The object to convert geometries into drawable shapes.
	 * @param gr The canvas to draw on.
	 */
	public abstract void draw(T f, int z, PointTransformation pt, Graphics2D gr);

	/**
	 * @param geom The geometry to draw.
	 * @return The footprint geometry of the object symbol.
	 */
	//public abstract Geometry footprint(Geometry st);

	private double xOffset=0;
	public double getxOffset() { return this.xOffset; }
	public Style<T> setxOffset(double xOffset) { this.xOffset = xOffset; return this; }
	private double yOffset=0;
	public double getyOffset() { return this.yOffset; }
	public Style<T> setyOffset(double yOffset) { this.yOffset = yOffset; return this; }
}
