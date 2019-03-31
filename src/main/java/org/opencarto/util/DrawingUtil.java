package org.opencarto.util;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.opencarto.style.PointTransformation;

/**
 * @author julien Gaffuri
 *
 */
public class DrawingUtil {

	public static final int CIRCLE_FILL = 0;
	public static final int SQUARE_FILL = 1;
	public static final int CIRCLE_OUTLINE = 2;
	public static final int SQUARE_OUTLINE = 3;

	public static void drawLine(LineString line, PointTransformation pt, Graphics2D gr) {
		drawLine(line, pt, gr, 0, 0);
	}

	public static void drawLine(LineString line, PointTransformation pt, Graphics2D gr, double xOffset, double yOffset) {
		Coordinate[] cs = line.getCoordinates();
		int nb = cs.length;
		GeneralPath path = new GeneralPath(Path2D.WIND_NON_ZERO, nb);
		boolean first = true;
		for(Coordinate c : cs){
			Point2D p = pt.geoToPix(c);
			if(first) {
				path.moveTo(p.getX() + xOffset, p.getY() + yOffset);
				first = false;
			}
			else
				path.lineTo(p.getX() + xOffset, p.getY() + yOffset);
		}
		gr.draw(path);
		/*
		Coordinate[] cs = line.getCoordinates();
		int nb = cs.length;
		int[] xs = new int[nb];
		int[] ys = new int[nb];
		int i=0;
		for(Coordinate c : cs){
			Point2D p = pt.geoToPix(c);
			xs[i]=(int) (p.getX() + xOffset);
			ys[i]=(int) (p.getY() + yOffset);
			i++;
		}
		gr.drawPolyline(xs, ys, nb);
		 */
	}


	public static void drawPolygon(Polygon poly, PointTransformation pt, Graphics2D gr) {
		drawPolygon(poly, pt, gr, 0, 0);
	}

	public static void drawPolygon(Polygon poly, PointTransformation pt, Graphics2D gr, double xOffset, double yOffset) {
		drawLine(poly.getExteriorRing(), pt, gr, xOffset, yOffset);
		for(int i=0; i<poly.getNumInteriorRing(); i++)
			drawLine(poly.getInteriorRingN(i), pt, gr, xOffset, yOffset);
	}

	public static void fillPolygon(Polygon poly, PointTransformation pt, Graphics2D gr) {
		fillPolygon(poly, pt, gr, 0, 0);
	}

	public static void fillPolygon(Polygon poly, PointTransformation pt, Graphics2D gr, double xOffset, double yOffset) {
		Coordinate[] cs = poly.getCoordinates();
		int nb = cs.length;
		GeneralPath path = new GeneralPath(Path2D.WIND_EVEN_ODD, nb);
		boolean first = true;
		for(Coordinate c : cs){
			Point2D p = pt.geoToPix(c);
			if(first) {
				path.moveTo(p.getX() + xOffset, p.getY() + yOffset);
				first = false;
			}
			else
				path.lineTo(p.getX() + xOffset, p.getY() + yOffset);
		}
		gr.fill(path);

		/*Coordinate[] cs = poly.getCoordinates();
		int nb = cs.length;
		int[] xs = new int[nb];
		int[] ys = new int[nb];
		int i=0;
		for(Coordinate c : cs){
			Point2D p = pt.geoToPix(c);
			xs[i]=(int) (p.getX() + xOffset);
			ys[i]=(int) (p.getY() + yOffset);
			i++;
		}
		gr.fillPolygon(xs, ys, nb);*/
	}

	public static void drawPoint(Coordinate c, PointTransformation pt, Graphics2D gr) {
		drawPoint(c, pt, gr, 4);
	}
	public static void drawPoint(Coordinate c, PointTransformation pt, Graphics2D gr, double size) {
		drawPoint(c, pt, gr, size, CIRCLE_FILL);
	}
	public static void drawPoint(Coordinate c, PointTransformation pt, Graphics2D gr, double size, int symbol) {
		drawPoint(c, pt, gr, size, symbol, 0, 0);
	}
	public static void drawPoint(Coordinate c, PointTransformation pt, Graphics2D gr, double size, int symbol, double xOffset, double yOffset) {
		Point2D p_ = pt.geoToPix(c);
		switch (symbol) {
		case CIRCLE_FILL:
			//gr.fillOval( (int)(p_.getX()-size/2+xOffset+0.5), (int)(p_.getY()-size+yOffset+0.5), size, size );
			gr.fill(new Ellipse2D.Double(p_.getX()-size*0.5+xOffset, p_.getY()-size*0.5+yOffset, size, size));
			break;
		case SQUARE_FILL:
			//gr.fillRect( (int)(p_.getX()-size/2+xOffset+0.5), (int)(p_.getY()-size+yOffset+0.5), size, size );
			gr.fill(new RoundRectangle2D.Double(p_.getX()-size*0.5+xOffset, p_.getY()-size*0.5+yOffset, size, size, 0, 0));
			break;
		case CIRCLE_OUTLINE:
			//gr.fillOval( (int)(p_.getX()-size/2+xOffset+0.5), (int)(p_.getY()-size+yOffset+0.5), size, size );
			gr.draw(new Ellipse2D.Double(p_.getX()-size*0.5+xOffset, p_.getY()-size*0.5+yOffset, size, size));
			break;
		case SQUARE_OUTLINE:
			//gr.fillRect( (int)(p_.getX()-size/2+xOffset+0.5), (int)(p_.getY()-size+yOffset+0.5), size, size );
			gr.draw(new RoundRectangle2D.Double(p_.getX()-size*0.5+xOffset, p_.getY()-size*0.5+yOffset, size, size, 0, 0));
			break;
		default:
			System.out.println("Unknown point symbol code: " + symbol);
			break;
		}
	}
}
