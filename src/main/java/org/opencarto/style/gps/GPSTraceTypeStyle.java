/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opencarto.gps.datamodel.GPSTrace;
import org.opencarto.style.ColorScale;
import org.opencarto.style.DrawingUtil;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;

/**
 * @author julien Gaffuri
 *
 */
public class GPSTraceTypeStyle extends Style<GPSTrace> {
	ColorScale<Double> colScale = null;
	Stroke stroke;

	public GPSTraceTypeStyle(ColorScale<Double> colScale, float w){
		this.colScale = colScale;
		stroke = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	}

	@Override
	public void draw(GPSTrace trace, int z, PointTransformation pt, Graphics2D gr) {
		//TODO draw by segment!
		Geometry geom = trace.getGeom(z);
		if(!(geom instanceof LineString)) return;
		double mSpeed = trace.getMeanSpeedKmH();
		Color col = colScale.getColor(mSpeed);
		gr.setColor(col);
		gr.setStroke(stroke);
		DrawingUtil.drawLine((LineString)geom, pt, gr,getxOffset(), getyOffset());
	}

}
