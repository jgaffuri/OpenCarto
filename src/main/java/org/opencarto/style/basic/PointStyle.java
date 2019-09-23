package org.opencarto.style.basic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Point;
import org.opencarto.datamodel.MultiScaleFeature;
import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.util.DrawingUtil;

/**
 * @author julien Gaffuri
 *
 */
public class PointStyle<T extends MultiScaleFeature> extends Style<T> {
	//private final static Logger logger = Logger.getLogger(PointStyle.class.getName());

	private int iconStyle = DrawingUtil.CIRCLE_FILL;
	public void setIconStyle(int iconStyle) { this.iconStyle = iconStyle; }

	private Color color = Color.RED;
	public Color getColor() { return this.color; }
	public void setColor(Color color) { this.color = color; }

	private float size = 3;
	public void setSize(float size) { this.size = size; }

	private float borderSize = 1;
	public void setBorderSize(float borderSize) {
		this.borderSize = borderSize;
		this.stroke = null;
	}

	private BasicStroke stroke=null;
	private BasicStroke getStroke() {
		if(this.stroke == null)
			this.stroke = new BasicStroke(borderSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND );
		return this.stroke;
	}

	private URL iconImageURL = null;
	public void setIconImageURL(String iconImageURL) {
		try {
			this.iconImageURL = new URL(iconImageURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		icon = null;
	}

	private BufferedImage icon = null;
	private BufferedImage getIcon() {
		if(icon==null)
			if(iconImageURL != null)
				try {
					icon = ImageIO.read(iconImageURL);
				} catch (IOException e) {
					e.printStackTrace();
				}
		return this.icon;
	}

	@Override
	public void draw(MultiScaleFeature f, int z, PointTransformation pt, Graphics2D gr) {
		Geometry geom = f.getGeom(z);
		if ( geom instanceof Point ) draw(geom.getCoordinate(), pt, gr);
		//else if ( geom instanceof Polygon ) draw((Polygon)geom, pt, gr);
		else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);

		//Coordinate[] cs = st.getGeom().getCoordinates();
		//for(int i=0; i<cs.length; i++)
		//	draw(cs[i], pt, gr);
	}

	private void draw(Coordinate c, PointTransformation pt, Graphics2D gr) {
		if(iconImageURL != null) {
			Point2D p1 = pt.geoToPix(c);
			gr.drawImage( getIcon(), (int)(p1.getX()+0.5+getxOffset()), (int)(p1.getY()+0.5+getyOffset()), null);
		} else {
			gr.setColor(getColor());
			gr.setStroke(getStroke());
			DrawingUtil.drawPoint(c, pt, gr, size, iconStyle, getxOffset(), getyOffset());
		}
	}

	private void draw(GeometryCollection gc, PointTransformation pt, Graphics2D gr) {
		for(int i=0; i<gc.getNumGeometries(); i++) {
			Geometry geom = gc.getGeometryN(i);
			if( geom instanceof Point ) draw(geom.getCoordinate(), pt, gr);
			else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);
		}
	}
}
