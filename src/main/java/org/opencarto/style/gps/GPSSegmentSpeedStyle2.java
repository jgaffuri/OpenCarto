/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.locationtech.jts.geom.LineString;
import org.opencarto.datamodel.Feature;
import org.opencarto.style.ColorScale;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.util.DrawingUtil;

/**
 * @author julien Gaffuri
 *
 */
public class GPSSegmentSpeedStyle2 extends Style<Feature> {
	ColorScale<Double> colScale = null;
	Stroke stroke;

	public GPSSegmentSpeedStyle2(ColorScale<Double> colScale, float w){
		this.colScale = colScale;
		stroke=new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
	}

	@Override
	public void draw(Feature seg, int z, PointTransformation pt, Graphics2D gr) {
		gr.setStroke(stroke);
		gr.setColor( colScale.getColor( Double.parseDouble(seg.getAttribute("s").toString()) ) );
		DrawingUtil.drawLine((LineString)seg.getDefaultGeometry(), pt, gr,getxOffset(), getyOffset());
	}

}
