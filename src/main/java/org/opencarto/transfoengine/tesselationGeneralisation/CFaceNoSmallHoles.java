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
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Polygon;

/**
 * Ensures small holes are deleted.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceNoSmallHoles extends Constraint {

	private double minSizeDel;

	public CFaceNoSmallHoles(Agent agent, double minSizeDel) {
		super(agent);
		this.minSizeDel=minSizeDel;
	}

	private Collection<Edge> tooSmallHoles;

	@Override
	public void computeCurrentValue() {
		Face d = (Face)(getAgent().getObject());

		//get exterior ring area
		Polygon poly = d.getGeometry();
		double outArea = poly.getFactory().createPolygon(poly.getExteriorRing().getCoordinates()).getArea();

		//find edges corresponding to holes
		//holes are closed and coastal edges which are not the outer ring
		tooSmallHoles = new HashSet<Edge>();
		for(Edge e : d.getEdges()){
			if(!e.isClosed()) continue;
			if(!e.isCoastal()) continue;
			double area = e.getArea();
			if(area>minSizeDel) continue;
			if(area==outArea) continue;
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
	public List<Transformation<?>> getTransformations() {
		ArrayList<Transformation<?>> out = new ArrayList<Transformation<?>>();

		//propose deletion of holes
		out.add(new TFaceHolesDeletion((AFace)getAgent(), tooSmallHoles));

		return out;
	}

}
