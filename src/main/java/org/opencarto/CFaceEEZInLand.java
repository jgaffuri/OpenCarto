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

	boolean shouldBeDeleted = false;

	@Override
	public void computeCurrentValue() {
		shouldBeDeleted = false;
		AFace aFace = getAgent();
		if(aFace.isDeleted()) return;
		if(!aFace.removalAllowed()) return;
		if(aFace.aUnit == null) return;
		if(!aFace.aUnit.getObject().id.contains("EEZ")) return;
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
		if(shouldBeDeleted) {
			//AFace af = getAgent();
			//Face f = af.getObject();
			//System.out.println(af.aUnit.getObject().id);
			//System.out.println(f.getGeometry().getCentroid().getCoordinate());
			//getTouchingFaces()
			out.add(new TFaceAggregation(getAgent()));
		}
		return out;
	}

}
