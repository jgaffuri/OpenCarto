/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.opencarto.datamodel.gps.GPSSegment;
import org.opencarto.style.ColorScale;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.util.DrawingUtil;

import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class GPSSegmentSpeedStyle extends Style<GPSSegment> {
	ColorScale<Double> colScale = null;
	Stroke stroke;

	public GPSSegmentSpeedStyle(ColorScale<Double> colScale, float w){
		this.colScale = colScale;
		stroke=new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	}

	@Override
	public void draw(GPSSegment seg, int z, PointTransformation pt, Graphics2D gr) {
		gr.setStroke(stroke);
		gr.setColor( colScale.getColor( seg.getMeanSpeedKmH() ) );
		DrawingUtil.drawLine((LineString)seg.getGeom(), pt, gr,getxOffset(), getyOffset());
	}

}
