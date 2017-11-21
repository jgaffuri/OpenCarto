/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Polygon;

/**
 * Ensures a face does not contain too small holes.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceNoSmallHoles extends Constraint<AFace> {

	private double minSizeDel;

	public CFaceNoSmallHoles(AFace agent, double minSizeDel) {
		super(agent);
		this.minSizeDel=minSizeDel;
	}

	private Collection<Edge> tooSmallHoles = null;

	@Override
	public void computeCurrentValue() {
		Face f = getAgent().getObject();
		tooSmallHoles = new HashSet<Edge>();

		if(getAgent().isDeleted()) return;

		//get exterior ring area
		Polygon poly = f.getGeometry();
		double outArea = 0;
		outArea = poly.getFactory().createPolygon(poly.getExteriorRing().getCoordinates()).getArea();

		//find edges corresponding to holes
		//holes are closed and coastal edges which are not the outer ring
		for(Edge e : f.getEdges()){
			if(!e.isClosed()) continue;
			if(!e.isCoastal()) continue;
			double area = e.getArea();
			if(area>minSizeDel) continue;
			if(area == outArea) continue;
			tooSmallHoles.add(e);
		}
	}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted())
			satisfaction = 10;
		else
			satisfaction = tooSmallHoles.size()>0? 0 : 10;
	}

	@Override
	public List<Transformation<AFace>> getTransformations() {
		ArrayList<Transformation<AFace>> out = new ArrayList<Transformation<AFace>>();

		//propose deletion of holes
		if(tooSmallHoles.size()>0)
			out.add(new TFaceHolesDeletion(getAgent(), tooSmallHoles));

		return out;
	}

}
