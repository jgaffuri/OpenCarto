/**
 * 
 */
package org.opencarto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.transfoengine.tesselationGeneralisation.AFace;
import org.opencarto.transfoengine.tesselationGeneralisation.AUnit;
import org.opencarto.transfoengine.tesselationGeneralisation.TFaceAggregation;

/**
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceEEZInLand extends Constraint<AFace> {

	public CFaceEEZInLand(AFace agent) { super(agent); }

	private boolean isEEZEnclave(AFace af) {
		//f is a EZZ enclave if:
		// - it is not coastal.
		Face f = af.getObject();
		if(f.isCoastal()) return false;
		// - it is not surrounded by another EEZ face.
		// - all surrounded faces are linked to the same unit
		Collection<Face> tfs = f.getTouchingFaces();
		String unitId = null;
		for(Face tf : tfs) {
			AUnit aunit = af.getAtesselation().getAFace(tf).aUnit;
			if(aunit == null) return false;
			String id = aunit.getObject().id;
			if(id.contains("EEZ")) return false;
			if(unitId == null)
				unitId = id;
			else
				if(!unitId.equals(id)) return false;
		}
		return true;
	}

	@Override
	public void computeSatisfaction() {
		AFace aFace = getAgent();
		if(aFace.isDeleted()) satisfaction = 10;
		else if(aFace.aUnit == null) satisfaction = 10;
		else if(!aFace.aUnit.getObject().id.contains("EEZ")) satisfaction = 10;
		else if(!isEEZEnclave(aFace)) satisfaction = 10;
		else if(!aFace.removalAllowed()) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AFace>> getTransformations() {
		ArrayList<Transformation<AFace>> out = new ArrayList<Transformation<AFace>>();
		out.add(new TFaceAggregation(getAgent()));
		return out;
	}

}
