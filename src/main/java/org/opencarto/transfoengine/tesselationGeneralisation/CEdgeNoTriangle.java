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
 * Ensure an edge does not become a triangle.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeNoTriangle extends Constraint {

	public CEdgeNoTriangle(Agent agent) {
		super(agent,null);
	}

	boolean isTriangleIni = false;
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
	public void computeInitialValue() {
		computeCurrentValue();
		isTriangleIni = isTriangle;
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = isTriangleIni? 10 : isTriangle? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

	@Override
	public List<Transformation<?>> getTransformations() {
		return new ArrayList<Transformation<?>>();
	}

}
