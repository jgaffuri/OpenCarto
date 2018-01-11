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
	//private final static Logger LOGGER = Logger.getLogger(CFaceSize.class.getName());

	private double minSizeDel, minSizeDelHole, minSize;

	/**
	 * @param agent
	 * @param minSizeDel Below this size, the face is always deleted. Above, it is enlarged to minSize or deleted if not possible
	 * @param minSizeDelHoles Below this size, the hole always is deleted. Above, it is enlarged to minSize or deleted if not possible
	 * @param minSize The minimum size of a face
	 * @param preserveAllUnits Ensure that no unit disappear. At least one face of a unit is preserved.
	 */
	public CFaceSize(AFace agent, double minSizeDel, double minSizeDelHole, double minSize, boolean preserveAllUnits) {
		super(agent);
		this.minSizeDel = minSizeDel;
		this.minSizeDelHole = minSizeDelHole;
		this.minSize = minSize;
		this.preserveAllUnits = preserveAllUnits;
	}

	boolean preserveAllUnits = true;
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

		AFace aFace = getAgent();
		Face f = aFace.getObject();

		//deletion case
		if(goalArea == 0 && aFace.removalAllowed()) {
			if(f.isIsland())
				//propose deletion
				out.add(new TFaceIslandDeletion(aFace));
			else
				//propose aggregation
				out.add(new TFaceAggregation(aFace));

		} else {
			//face size should be changed to goalSize. If not possible and still smaller than minimum threshold, should be deleted
			//try to scale, if allowed
			if(!aFace.hasFrozenEdge()) {
				if(f.isIsland() || f.isEnclave()) {
					for(double k : new double[]{1, 0.8, 0.5, 0.02})
						out.add(new TFaceScaling(aFace, k*Math.sqrt(goalArea/currentArea)));
				} else {
					//TODO scaling/deformation for non islands and non enclave
				}
			}
			//if too small, try to delete
			if(goalArea<minSize) {
				System.out.println("aaa");
				if(f.isIsland()) out.add(new TFaceIslandDeletion(aFace));
				else {
					out.add(new TFaceAggregation(aFace));
				}
			}
		}

		return out;
	}

}
