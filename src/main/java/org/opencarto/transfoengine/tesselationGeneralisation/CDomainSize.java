/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CDomainSize extends Constraint {

	private double minSizeDel, minSize;

	public CDomainSize(Agent agent, double minSizeDel, double minSize) {
		super(agent);
		this.minSizeDel=minSizeDel;
		this.minSize=minSize;
	}



	double initialValue, currentValue, goalValue;

	@Override
	public void computeInitialValue() {
		computeCurrentValue();
		initialValue = currentValue;
	}

	@Override
	public void computeCurrentValue() {
		Face d = (Face)(getAgent().getObject());
		currentValue = d.getGeometry()==null? 0 : d.getGeometry().getArea();
	}

	@Override
	public void computeGoalValue() {
		goalValue = initialValue>minSize ? initialValue : initialValue<minSizeDel? 0 : minSize;
	}




	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted())
			if(goalValue == 0) satisfaction=10; else satisfaction=0;
		else
			if(goalValue == 0) satisfaction=0;
			else satisfaction = 10 - 10*Math.abs(goalValue-currentValue)/goalValue;
	}

	@Override
	public List<Transformation<?>> getTransformations() {
		ArrayList<Transformation<?>> out = new ArrayList<Transformation<?>>();
		
		//deletion case
		if(goalValue == 0){
			ADomain aDom = (ADomain)getAgent();
			Face dom = aDom.getObject();

			//islands case
			if(dom.isIsland())
				if( aDom.isTheLastUnitPatchToRemove() ){}
				else{
					//propose deletion
					out.add(new TIslandDomainDeletion(aDom));
					//TODO propose also amalgamation for islands sharing a straight
				}

			
			//enclave case
			else if(dom.isEnclave())
				if( aDom.isTheLastUnitPatchToRemove() ){}
				else{
					//propose deletion
					out.add(new TEnclaveDomainDeletion(aDom));
					//TODO propose also amalgamation for enclaves with narrow corridor
				}


			//other case
			else {
				//TODO
				//check if good aggregation candidate exists. If yes, aggregate else collapse
			}
		}
		return out;
	}

}
