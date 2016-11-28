package org.opencarto.style.basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.opencarto.datamodel.Feature;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.util.DrawingUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;

/**
 * @author julien Gaffuri
 *
 */
public class LineStyle extends Style<Feature> {
	//private final static Logger logger = Logger.getLogger(LineStyle.class.getName());

	private Color color = Color.RED;
	public Color getColor() { return this.color; }
	public LineStyle setColor(Color color) { this.color = color; return this; }

	private float width = 1;
	public LineStyle setWidth(float width) {
		this.width = width;
		this.stroke = null;
		return this;
	}

	private int cap = BasicStroke.CAP_BUTT;
	public LineStyle setCap(int cap) {
		this.cap = cap;
		stroke = null;
		return this;
	}

	private int join = BasicStroke.JOIN_ROUND;
	public LineStyle setJoin(int join) {
		this.join = join;
		stroke = null;
		return this;
	}

	public LineStyle setDashSize(float d) {
		this.dashFillSize = d;
		this.dashBlankSize = d;
		stroke = null;
		return this;
	}

	private float dashFillSize = 0f;
	public LineStyle setDashFillSize(float d) {
		this.dashFillSize = d;
		stroke = null;
		return this;
	}

	private float dashBlankSize = 0f;
	public LineStyle setDashBlankSize(float d) {
		this.dashBlankSize = d;
		stroke = null;
		return this;
	}

	private BasicStroke stroke=null;
	private BasicStroke getStroke() {
		if(this.stroke == null)
			if(dashFillSize <= 0 || dashBlankSize <= 0 )
				this.stroke = new BasicStroke(width, cap, join);
			else
				this.stroke = new BasicStroke(width, cap, join, 10f, new float[]{dashFillSize,dashBlankSize}, 0f);
		return this.stroke;
	}

	@Override
	public void draw(Feature f, int z, PointTransformation pt, Graphics2D gr) {
		Geometry geom = f.getGeom(z);
		if ( geom instanceof LineString ) draw((LineString)geom, pt, gr);
		else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);
	}

	private void draw(GeometryCollection gc, PointTransformation pt, Graphics2D gr) {
		for(int i=0; i<gc.getNumGeometries(); i++) {
			Geometry geom = gc.getGeometryN(i);
			if( geom instanceof LineString ) draw((LineString)geom, pt, gr);
			else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);
		}
	}

	void draw(LineString line, PointTransformation pt, Graphics2D gr) {
		gr.setColor(getColor());
		gr.setStroke(getStroke());
		DrawingUtil.drawLine(line, pt, gr,getxOffset(), getyOffset());
	}

}
