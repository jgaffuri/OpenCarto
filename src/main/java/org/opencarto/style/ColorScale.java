/**
 * 
 */
package org.opencarto.style;

import java.awt.Color;

/**
 * @author Julien Gaffuri
 *
 */
public interface ColorScale<T> {

	public Color getColor(T value);

}
