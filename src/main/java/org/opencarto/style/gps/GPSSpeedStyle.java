/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.opencarto.datamodel.gps.GPSSegment;
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
		gr.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		for(GPSSegment seg : trace.getSegments()){
			Geometry geom = seg.getGeometry();
			if(!(geom instanceof LineString)) return;
			double mSpeed = seg.getMeanSpeedKmH();
			Color col = colScale.getColor(mSpeed);
			gr.setColor(col);
			DrawingUtil.drawLine((LineString)geom, pt, gr,getxOffset(), getyOffset());
		}
	}

}
