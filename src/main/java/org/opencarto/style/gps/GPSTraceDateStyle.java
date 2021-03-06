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
public class GPSTraceDateStyle extends Style<GPSTrace> {
	ColorScale<Long> colScale = null;
	Stroke stroke;

	public GPSTraceDateStyle(ColorScale<Long> colScale, float w){
		this.colScale = colScale;
		stroke = new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	}

	@Override
	public void draw(GPSTrace trace, int z, PointTransformation pt, Graphics2D gr) {
		Geometry geom = trace.getGeom(z);
		if(!(geom instanceof LineString)) return;

		if(trace.getStartTime() == null) gr.setColor( Color.gray );
		else if(trace.getStartTime().getDate() == null) gr.setColor( Color.gray );
		else gr.setColor( colScale.getColor(trace.getStartTime().getDate().getTime()) );

		gr.setStroke(stroke);
		DrawingUtil.drawLine((LineString)geom, pt, gr,getxOffset(), getyOffset());
	}

}
