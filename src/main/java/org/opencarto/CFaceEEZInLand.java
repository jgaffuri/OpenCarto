/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.TFaceAggregation;

/**
 * 
 * Ensure faces are large enougth.
 * Too small faces are deleted, aggregated of scaled.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceEEZInLand extends Constraint<AFace> {

	public CFaceEEZInLand(AFace agent) { super(agent); }

	@Override
	public void computeCurrentValue() {}

	boolean shouldBeDeleted = false;
	@Override
	public void computeGoalValue() {
		shouldBeDeleted = false;
		AFace aFace = getAgent();
		if(!aFace.removalAllowed()) return;
		if(!aFace.aUnit.getId().contains("EEZ")) return;
		if(!aFace.getObject().isEnclave()) return;
		//TODO get neighbour face and check it is not a EEZ?
		shouldBeDeleted = true;
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = shouldBeDeleted && getAgent().removalAllowed() && !getAgent().isDeleted() ? 0 : 10;
	}

	@Override
	public List<Transformation<AFace>> getTransformations() {
		ArrayList<Transformation<AFace>> out = new ArrayList<Transformation<AFace>>();

		AFace aFace = getAgent();

		if(shouldBeDeleted && aFace.removalAllowed()) {
			//propose aggregation
			out.add(new TFaceAggregation(aFace));
		}

		return out;
	}

}
