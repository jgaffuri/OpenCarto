/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.transfoengine.TransformationNonCancellable;

/**
 * 
 * Constraint ensuring that a unit has no narrow gap.
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNarrowPartsAndGapsXXX extends Constraint<AUnit> {
	//private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowPartsAndCorridors.class);

	public CUnitNarrowPartsAndGapsXXX(AUnit agent) {
		super(agent);
	}

	private int initialNumber, number = 0;

	@Override
	public void computeInitialValue() {
		computeCurrentValue();
		initialNumber = number;
	}

	@Override
	public void computeCurrentValue() {
		number = getAgent().narrowGaps == null? 0 : getAgent().narrowGaps.size();
	}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) satisfaction = 10;
		else if(initialNumber == 0) satisfaction = 10;
		else satisfaction = 10*(1-number/initialNumber);
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		ArrayList<Transformation<AUnit>> out = new ArrayList<Transformation<AUnit>>();

		out.add(new TransformationNonCancellable<AUnit>((AUnit)getAgent()) {

			@Override
			public void apply() {
				AUnit au = getAgent();

				try {
					au.absorbGaps();
				} catch (Exception e) {
					System.err.println("Failed absorbing straits for "+au.getId() + "  "+e.getMessage());
				}
			}

			public String toString(){ return getClass().getSimpleName(); }
		});

		return out;
	}

}
