/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.TFaceAggregation;

/**
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceEEZInLand extends Constraint<AFace> {

	public CFaceEEZInLand(AFace agent) { super(agent); }

	boolean toBeDeleted;

	@Override
	public void computeCurrentValue() {
		toBeDeleted = false;
		AFace aFace = getAgent();
		if(aFace.isDeleted()) return;
		if(!aFace.removalAllowed()) return;
		if(aFace.aUnit == null) return;
		if(!aFace.aUnit.getObject().id.contains("EEZ")) return;
		if(!aFace.getObject().isEnclave()) return; //TODO inmprove - consider enclave within land
		//TODO get neighbour face and check it is not a EEZ?
		toBeDeleted = true;
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = toBeDeleted && !getAgent().isDeleted() && getAgent().removalAllowed() ? 0 : 10;
	}

	@Override
	public List<Transformation<AFace>> getTransformations() {
		ArrayList<Transformation<AFace>> out = new ArrayList<Transformation<AFace>>();
		if(toBeDeleted)
			out.add(new TFaceAggregation(getAgent()));
		return out;
	}

}
