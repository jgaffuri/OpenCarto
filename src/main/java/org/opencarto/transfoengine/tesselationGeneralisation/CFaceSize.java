/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
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
	private final static Logger LOGGER = Logger.getLogger(CFaceSize.class.getName());

	private double minSizeDel, minSizeDelHole, minSize;

	/**
	 * @param agent
	 * @param minSizeDel Below this size, the face is deleted. Above, it is enlarged until minSize
	 * @param minSizeDelHoles Below this size, the hole is deleted. Above, it is enlarged until minSize
	 * @param minSize The minimum size of a face
	 */
	public CFaceSize(AFace agent, double minSizeDel, double minSizeDelHole, double minSize) {
		super(agent);
		this.minSizeDel = minSizeDel;
		this.minSizeDelHole = minSizeDelHole;
		this.minSize = minSize;
	}


	double initialArea, currentArea, goalArea;

	@Override
	public void computeInitialValue() {
		computeCurrentValue();
		initialArea = currentArea;
	}

	@Override
	public void computeCurrentValue() {
		Face d = (Face)(getAgent().getObject());
		currentArea = d.getGeometry()==null? 0 : d.getGeometry().getArea();
	}

	@Override
	public void computeGoalValue() {
		AFace aFace = getAgent();
		double del = aFace.isHole()? minSizeDelHole : minSizeDel;
		goalArea = (initialArea<del && aFace.removalAllowed())? 0 : initialArea<minSize ? minSize : initialArea;
	}



	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted())
			if(goalArea == 0) satisfaction=10; else satisfaction=0;
		else
			if(goalArea == 0) satisfaction=0;
			else satisfaction = 10 - 10*Math.abs(goalArea-currentArea)/goalArea;
		if(satisfaction<0) satisfaction=0;
	}

	@Override
	public List<Transformation<AFace>> getTransformations() {
		ArrayList<Transformation<AFace>> out = new ArrayList<Transformation<AFace>>();

		AFace aFace = (AFace)getAgent();
		Face f = aFace.getObject();

		//deletion case
		if(goalArea == 0 && aFace.removalAllowed()) {
			if(f.isIsland()){
				//islands case
				//propose face deletion
				out.add(new TFaceIslandDeletion(aFace));
			} else {
				//determine best surrounding face to aggregate with: the surrounding face with the longest boundary
				//TODO improve candidate selection method (maybe the other face's size could also be considered?)
				//TODO propose also face collapse if several equivalent candidates are found.
				Face bestCandidateFace = null;
				double maxLength=-1;
				for(Face f2 : f.getTouchingFaces()){
					double length = f.getLength(f2);
					if(length<maxLength) continue;
					bestCandidateFace = f2; maxLength = length;
				}

				if(bestCandidateFace == null)
					LOGGER.error("Could not find good candidate face for aggregation of face "+f.getId()+". Number of edges: "+f.getEdges().size());
				else
					//propose aggregation
					out.add(new TFaceAggregation(aFace, bestCandidateFace));
			}

		} else
			//scaling case
			//System.out.println(f.isEnclave());
			if(f.isIsland() || f.isEnclave()){
				//propose scalings
				if(!aFace.hasFrozenEdge())
					for(double k : new double[]{1, 0.8, 0.5, 0.02}) {
						//System.out.println(aFace.getId());
						out.add(new TFaceScaling(aFace, k*Math.sqrt(goalArea/currentArea)));
					}
				if(goalArea<minSize){
					//in such case, if scaling does not work, propose also deletion
					if(f.isIsland()) out.add(new TFaceIslandDeletion(aFace));
					if(f.isEnclave()) out.add(new TFaceAggregation(aFace, f.getTouchingFaces().iterator().next()));
				}
			} else {
				//TODO propose size change (scaling/deformation)
			}
		return out;
	}

}
