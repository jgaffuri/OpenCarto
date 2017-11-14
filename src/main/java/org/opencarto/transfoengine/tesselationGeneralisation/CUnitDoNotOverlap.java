/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CUnitDoNotOverlap  extends Constraint<AUnit> {

	Collection<Intersection> inters;

	public CUnitDoNotOverlap(AUnit agent) { super(agent); }

	@Override
	public void computeCurrentValue() {
		inters = new HashSet<Intersection>();
		//getAgent().getObject()
		//TODO retrieve all units overlapping, with spatial index
	}

	@Override
	public void computeSatisfaction() {
		if(inters == null || inters.size()==0) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
	}

	
	public class Intersection{
		String id;
		double area;
		double percentage;
	}
	
}
