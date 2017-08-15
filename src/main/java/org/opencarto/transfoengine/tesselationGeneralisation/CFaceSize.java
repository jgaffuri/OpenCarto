/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CFaceSize extends Constraint {

	private double minSizeDel, minSize;

	public CFaceSize(Agent agent, double minSizeDel, double minSize) {
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
			AFace aFace = (AFace)getAgent();
			Face f = aFace.getObject();


			//islands case
			if(f.isIsland()){
				if( ! aFace.isTheLastUnitPatchToRemove() ) {
					//propose deletion
					out.add(new TFaceIslandDeletion(aFace));
					//TODO propose also amalgamation for islands sharing a straight
				}
			}


			//enclave case
			else if(f.isEnclave()){
				if( ! aFace.isTheLastUnitPatchToRemove() ) {
					//propose deletion
					out.add(new TFaceEnclaveDeletion(aFace));
					//TODO propose also amalgamation for enclaves with narrow corridor
				}
			}


			//other case
			else {
				if( ! aFace.isTheLastUnitPatchToRemove() ) {
					//check if good aggregation candidate exists. If yes, aggregate else collapse

					//best edge for aggregation is the one having the maximum length and having another face. Maybe the other face size could also be considered?
					Edge maxLengthEdge=null; double maxLength=-1;
					for(Edge e:f.getEdges()){
						if(e.getFaces().size()<2) continue;
						double length = e.getGeometry().getLength();
						if(length<maxLength) continue;
						maxLengthEdge = e; maxLength = length;
					}

					if(maxLengthEdge==null) System.err.println("Could not find good candidate face for aggregation of face "+f.getId()+". Maybe this face is an island?");

					//propose aggregation
					Face otherFace = maxLengthEdge.f1==f? maxLengthEdge.f2 : maxLengthEdge.f1;
					out.add(new TFaceAggregation(aFace, otherFace, maxLengthEdge));

					//TODO improve candidate selection method and propose also face collapse if several equivalent candidates are found.
				}
			}

		}
		return out;
	}

}
