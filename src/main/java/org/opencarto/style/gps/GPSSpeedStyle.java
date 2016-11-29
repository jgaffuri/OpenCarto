/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.style.ColorScale;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.util.DrawingUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class GPSSpeedStyle extends Style<GPSTrace> {
	ColorScale colScale = null;
	float width;

	public GPSSpeedStyle(ColorScale colScale, float w){ this.colScale = colScale; width=w; }

	@Override
	public void draw(GPSTrace trace, int z, PointTransformation pt, Graphics2D gr) {
		//TODO draw by segment!
		Geometry geom = trace.getGeom(z);
		if(!(geom instanceof LineString)) return;
		Color col = colScale.getColor(trace.getMeanSpeedKmH());
		gr.setColor(col);
		gr.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		DrawingUtil.drawLine((LineString)geom, pt, gr,getxOffset(), getyOffset());
	}

}
