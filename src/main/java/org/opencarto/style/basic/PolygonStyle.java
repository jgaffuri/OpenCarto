package org.opencarto.style.basic;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.opencarto.style.PointTransformation;
import org.opencarto.style.Style;
import org.opencarto.util.DrawingUtil;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author julien Gaffuri
 *
 */
public class PolygonStyle extends Style {
	private final static Logger logger = Logger.getLogger(PolygonStyle.class.getName());

	private Color fillColor = null;
	public Color getFillColor() { return this.fillColor; }
	public void setFillColor(Color fillColor) { this.fillColor = fillColor; }

	//gradient
	private float gradientSize = -1;
	public void setGradientSize(float gradientSize) { this.gradientSize = gradientSize; }

	private float gradientAngle = 0;
	public void setGradientAngle(float gradientAngle) { this.gradientAngle = gradientAngle; }

	private Color color1 = Color.BLUE;
	public Color getColor1() { return this.color1; }
	public void setColor1(Color color1) { this.color1 = color1; }
	private Color color2 = Color.RED;
	public Color getColor2() { return this.color2; }
	public void setColor2(Color color2) { this.color2 = color2; }

	private boolean cyclic = true;
	public void setCyclic(boolean cyclic) { this.cyclic = cyclic; }

	//texture
	private URL textureImageURL = null;
	public void setTextureImageURL(String textureImageURL) {
		try {
			this.textureImageURL = new URL(textureImageURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		texture = null;
	}

	private LineStyle lineStyle = null;
	public void setOutlineStyle(LineStyle lineStyle) { this.lineStyle = lineStyle; }

	private BufferedImage texture = null;
	private BufferedImage getTexture() {
		if(texture==null)
			if(textureImageURL != null)
				try {
					texture = ImageIO.read(textureImageURL);
				} catch (IOException e) {
					e.printStackTrace();
				}
		return this.texture;
	}


	@Override
	public void draw(Geometry geom, PointTransformation pt, Graphics2D gr) {
		if ( geom instanceof Polygon ) draw((Polygon)geom, pt, gr);
		else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);
	}

	private void draw(Polygon geom, PointTransformation pt, Graphics2D gr) {
		//fill
		if(getFillColor() != null){
			gr.setColor(getFillColor());
			DrawingUtil.fillPolygon(geom, pt, gr, getxOffset(), getyOffset());
		}

		//gradient
		if(gradientSize>0){
			Envelope env = geom.getEnvelopeInternal();
			Point2D p = pt.geoToPix(new Coordinate(env.getMinX(), env.getMinY()));
			double a=-gradientAngle*Math.PI/180;
			GradientPaint gp = new GradientPaint((float)p.getX(), (float)p.getY(), color1, (float)(p.getX()+gradientSize*Math.cos(a)), (float)(p.getY()+gradientSize*Math.sin(a)), color2, cyclic);
			gr.setPaint(gp);
			DrawingUtil.fillPolygon(geom, pt, gr, getxOffset(), getyOffset());
		}

		//texture
		if(textureImageURL != null){
			Envelope env = geom.getEnvelopeInternal();
			Point2D p = pt.geoToPix(new Coordinate(env.getMinX(), env.getMinY()));
			TexturePaint tp = new TexturePaint(getTexture(), new Rectangle2D.Double(p.getX(), p.getY(), getTexture().getWidth(), getTexture().getHeight()));
			gr.setPaint(tp);
			DrawingUtil.fillPolygon(geom, pt, gr, getxOffset(), getyOffset());
		}

		//outline
		if(lineStyle != null){
			lineStyle.draw(geom.getExteriorRing(), pt, gr);
			for(int i=0; i<geom.getNumInteriorRing(); i++)
				lineStyle.draw(geom.getInteriorRingN(i), pt, gr);
		}
	}

	private void draw(GeometryCollection gc, PointTransformation pt, Graphics2D gr) {
		for(int i=0; i<gc.getNumGeometries(); i++) {
			Geometry geom = gc.getGeometryN(i);
			if ( geom instanceof Polygon ) draw((Polygon)geom, pt, gr);
			else if ( geom instanceof GeometryCollection ) draw((GeometryCollection)geom, pt, gr);
		}
	}

}
