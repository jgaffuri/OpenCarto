/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.Color;

import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.style.ColorScale;
import org.opencarto.style.basic.LineStyle;

/**
 * @author julien Gaffuri
 *
 */
public class GPSSpeedStyle extends LineStyle<GPSTrace> {
	ColorScale col = null;

	public GPSSpeedStyle(ColorScale col){ this.col = col; }

}
