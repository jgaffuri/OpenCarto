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

/**
 * Ensures small holes are deleted
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

	Collection<Edge> tooSmallHoles;

	@Override
	public void computeCurrentValue() {
		Face d = (Face)(getAgent().getObject());
		//TODO detect too small holes
		tooSmallHoles = new HashSet<Edge>();
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
