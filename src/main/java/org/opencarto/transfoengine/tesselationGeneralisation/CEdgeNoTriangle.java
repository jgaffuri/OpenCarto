/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * Ensure an edge is not a triangle
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeNoTriangle extends Constraint {

	public CEdgeNoTriangle(Agent agent) {
		super(agent);
	}

	boolean isTriangle = false;

	@Override
	public void computeCurrentValue() {
		LineString g = ((Edge)getAgent().getObject()).getGeometry();
		if(g.isClosed() && g.getNumPoints()<=4)
			isTriangle = true;
		else
			isTriangle = false;
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = isTriangle? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<?>> getTransformations() {
		return new ArrayList<Transformation<?>>();
	}

}
