/**
 * 
 */
package org.opencarto.style.gps;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

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
	ColorScale colScale = null;
	float width;

	public GPSSegmentSpeedStyle(ColorScale colScale, float w){ this.colScale = colScale; width=w; }

	@Override
	public void draw(GPSSegment seg, int z, PointTransformation pt, Graphics2D gr) {
		gr.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		gr.setColor( colScale.getColor( seg.getMeanSpeedKmH() ) );
		DrawingUtil.drawLine((LineString)seg.getGeom(), pt, gr,getxOffset(), getyOffset());
	}

}
