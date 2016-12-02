/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Date;

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
public class GPSTraceDateStyle extends Style<GPSTrace> {
	ColorScale<Date> colScale = null;
	float width;

	public GPSTraceDateStyle(ColorScale<Date> colScale, float w){ this.colScale = colScale; width=w; }

	@Override
	public void draw(GPSTrace trace, int z, PointTransformation pt, Graphics2D gr) {
		Geometry geom = trace.getGeom(z);
		if(!(geom instanceof LineString)) return;
		Date date = trace.getStartTime().getDate();
		Color col = colScale.getColor(date);
		gr.setColor(col);
		gr.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		DrawingUtil.drawLine((LineString)geom, pt, gr,getxOffset(), getyOffset());
	}

}
