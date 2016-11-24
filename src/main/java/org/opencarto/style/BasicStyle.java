package org.opencarto.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencarto.util.DrawingUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * The basic style.
 * 
 * @author julien Gaffuri
 *
 */
public class BasicStyle extends Style {
	private final static Logger logger = Logger.getLogger(BasicStyle.class.getName());

	private Color borderColor=Color.GRAY;
	public Color getBorderColor() { return this.borderColor; }
	public void setBorderColor(Color borderColor) { this.borderColor = borderColor; }

	private float borderSize=2;
	public void setBorderSize(float borderSize) {
		this.borderSize = borderSize;
		this.stroke = null;
	}

	private Color fillColor = new Color(100,100,100,125);
	public Color getFillColor() { return this.fillColor; }
	public void setFillColor(Color fillColor) { this.fillColor = fillColor; }

	private BasicStroke stroke=null;
	private BasicStroke getStroke() {
		if(this.stroke == null) this.stroke = new BasicStroke(borderSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND );
		return this.stroke;
	}

	@Override
	public void draw(Geometry geom, PointTransformation pt, Graphics2D gr) {
		if( geom instanceof Point ) draw((Point)geom, pt, gr);
		else if ( geom instanceof LineString ) draw((LineString)geom, pt, gr);
		else if ( geom instanceof Polygon ) draw((Polygon)geom, pt, gr);
		else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);
		else logger.log(Level.WARNING, "Method not implemented yet for geometry type: " + geom.getClass().getSimpleName());
	}

	private void draw(Point geom, PointTransformation pt, Graphics2D gr) {
		gr.setStroke(this.getStroke());
		gr.setColor(getBorderColor());
		DrawingUtil.drawPoint(geom.getCoordinate(), pt, gr, 2+borderSize, DrawingUtil.CIRCLE_FILL, getxOffset(), getyOffset());
		gr.setColor(getFillColor());
		DrawingUtil.drawPoint(geom.getCoordinate(), pt, gr, 2, DrawingUtil.CIRCLE_FILL, getxOffset(), getyOffset());
	}

	private void draw(LineString geom, PointTransformation pt, Graphics2D gr) {
		gr.setColor(getBorderColor());
		gr.setStroke(getStroke());
		DrawingUtil.drawLine(geom, pt, gr, getxOffset(), getyOffset());
	}

	private void draw(Polygon geom, PointTransformation pt, Graphics2D gr) {
		gr.setColor(getFillColor());
		DrawingUtil.fillPolygon(geom, pt, gr, getxOffset(), getyOffset());
		gr.setStroke(getStroke());
		gr.setColor(getBorderColor());
		DrawingUtil.drawPolygon(geom, pt, gr, getxOffset(), getyOffset());
	}

	private void draw(GeometryCollection gc, PointTransformation pt, Graphics2D gr) {
		for(int i=0; i<gc.getNumGeometries(); i++) {
			Geometry geom = gc.getGeometryN(i);
			if(geom.isEmpty()) return;
			//TODO really necessary?
			else if( geom instanceof Point ) draw((Point)geom, pt, gr);
			else if ( geom instanceof LineString ) draw((LineString)geom, pt, gr);
			else if ( geom instanceof Polygon ) draw((Polygon)geom, pt, gr);
			else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);
		}
	}
}
