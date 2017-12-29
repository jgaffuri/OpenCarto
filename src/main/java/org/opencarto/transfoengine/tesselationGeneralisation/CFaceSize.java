/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Ensure faces are large enougth.
 * Too small faces are deleted, aggregated of scaled.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceSize extends Constraint<AFace> {

	private double minSizeDel, minSizeDelHoles, minSize;

	/**
	 * @param agent
	 * @param minSizeDel Below this size, the face is deleted
	 * @param minSizeDelHoles Below this size, the hole is deleted
	 * @param minSize The minimum size of a face
	 */
	public CFaceSize(AFace agent, double minSizeDel, double minSizeDelHoles, double minSize) {
		super(agent);
		this.minSizeDel=minSizeDel;
		this.minSizeDelHoles=minSizeDelHoles;
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
		AFace aFace = getAgent();
		if(aFace.isHole() || aFace.getObject().isIsland()){
			goalValue = initialValue>minSize ? initialValue : (initialValue<minSizeDelHoles)? 0 : minSize;
		} else {
			goalValue = initialValue>minSize ? initialValue : (initialValue<minSizeDel && aFace.removalAllowed())? 0 : minSize;
		}
	}



	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted())
			if(goalValue == 0) satisfaction=10; else satisfaction=0;
		else
			if(goalValue == 0) satisfaction=0;
			else satisfaction = 10 - 10*Math.abs(goalValue-currentValue)/goalValue;
		if(satisfaction<0) satisfaction=0;
	}

	@Override
	public List<Transformation<AFace>> getTransformations() {
		ArrayList<Transformation<AFace>> out = new ArrayList<Transformation<AFace>>();

		AFace aFace = (AFace)getAgent();
		Face f = aFace.getObject();

		//deletion case
		if(goalValue == 0 && aFace.removalAllowed()){
			if(f.isIsland()){
				//islands case
				//propose face deletion
				out.add(new TFaceIslandDeletion(aFace));
			} else {

				//determine best surrounding face to aggregate with
				//it is the surrounding face with the longest boundary
				//TODO improve candidate selection method (Maybe the other face's size could also be considered?)
				//TODO propose also face collapse if several equivalent candidates are found.
				Face bestCandidateFace = null;
				double maxLength=-1;
				for(Face f2:f.getTouchingFaces()){
					double length = f.getLength(f2);
					if(length<maxLength) continue;
					bestCandidateFace = f2; maxLength = length;
				}

				if(bestCandidateFace == null)
					System.err.println("Could not find good candidate face for aggregation of face "+f.getId()+". Number of edges: "+f.getEdges().size());
				else
					//propose aggregation
					out.add(new TFaceAggregation(aFace, bestCandidateFace));
			}

		} else if(goalValue>0) {
			if(f.isIsland() || f.isEnclave()){
				//propose scalings
				if(!aFace.hasFrozenEdge())
					for(double k : new double[]{1, 0.8, 0.5, 0.02})
						out.add(new TFaceScaling(aFace, k*Math.sqrt(goalValue/currentValue)));
				if(goalValue<minSize){
					//in such case, if scaling does not work, propose also deletion
					if(f.isIsland()) out.add(new TFaceIslandDeletion(aFace));
					if(f.isEnclave()) out.add(new TFaceAggregation(aFace, f.getTouchingFaces().iterator().next()));
				}
			} else {
				//TODO propose size change (scaling/deformation)
			}
		}
		return out;
	}

}
