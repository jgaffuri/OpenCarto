/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Constraint ensuring a unit's size is equal to a goal one, typically the initial value.
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoNarrowPartsAndCorridors extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowPartsAndCorridors.class);

	public CUnitNoNarrowPartsAndCorridors(AUnit agent) {
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
		number = getAgent().straits == null? 0 : getAgent().straits.size();
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

		out.add(new Transformation<AUnit>((AUnit)getAgent()) {

			@Override
			public void apply() {
				AUnit au = getAgent();

				try {
					au.absorbStraits();
				} catch (Exception e) {
					System.err.println("Failed absorbing straits for "+au.getId() + "  "+e.getMessage());
				}
			}

			@Override
			public boolean isCancelable() { return false; }
			@Override
			public void storeState() {}
			@Override
			public void cancel() { System.err.println("cancel() not implemented for "+this.getClass().getSimpleName()); }

		});

		return out;
	}

}
