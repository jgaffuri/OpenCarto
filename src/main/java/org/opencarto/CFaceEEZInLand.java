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
import org.opencarto.transfoengine.tesselationGeneralisation.TFaceIslandDeletion;
import org.opencarto.transfoengine.tesselationGeneralisation.TFaceScaling;

/**
 * 
 * Ensure faces are large enougth.
 * Too small faces are deleted, aggregated of scaled.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceEEZInLand extends Constraint<AFace> {

	public CFaceEEZInLand(AFace agent) {
		super(agent);
	}

	@Override
	public void computeCurrentValue() {}

	boolean shouldBeDeleted = false;

	@Override
	public void computeGoalValue() {
		AFace aFace = getAgent();
		double del = aFace.isHole()? minSizeDelHole : minSizeDel;
		goalArea = (initialArea<del && aFace.removalAllowed())? 0 : initialArea<minSize ? minSize : initialArea;
	}



	@Override
	public void computeSatisfaction() {
		satisfaction = shouldBeDeleted && !getAgent().isDeleted() ? 0 : 10;
	}

	@Override
	public List<Transformation<AFace>> getTransformations() {
		ArrayList<Transformation<AFace>> out = new ArrayList<Transformation<AFace>>();

		AFace aFace = getAgent();
		Face f = aFace.getObject();

		//deletion case
		if(shouldBeDeleted && aFace.removalAllowed()) {
			//propose aggregation
			out.add(new TFaceAggregation(aFace));
		}

		return out;
	}

}
