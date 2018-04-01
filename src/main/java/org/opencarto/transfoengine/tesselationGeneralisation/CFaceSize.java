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

import com.vividsolutions.jts.geom.Geometry;

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
	private boolean preserveAllUnits, preserveIfPointsInIt;

	/**
	 * @param agent
	 * @param minSizeDel Below this size, the face is always deleted. Above, it is enlarged to minSize or deleted if not possible
	 * @param minSizeDelHoles Below this size, the hole always is deleted. Above, it is enlarged to minSize or deleted if not possible
	 * @param minSize The minimum size of a face
	 * @param preserveAllUnits Ensure that no unit disappear. At least one face of a unit is preserved.
	 * @param preserveIfPointsInIt Ensure that the face is not deleted if it has points in it.
	 */
	public CFaceSize(AFace agent, double minSizeDel, double minSizeDelHole, double minSize, boolean preserveAllUnits, boolean preserveIfPointsInIt) {
		super(agent);
		this.minSizeDel = minSizeDel;
		this.minSizeDelHole = minSizeDelHole;
		this.minSize = minSize;
		this.preserveAllUnits = preserveAllUnits;
		this.preserveIfPointsInIt = preserveIfPointsInIt;
	}

	double initialArea, currentArea, goalArea;

	@Override
	public void computeInitialValue() {
		computeCurrentValue();
		initialArea = currentArea;
	}

	@Override
	public void computeCurrentValue() {
		Geometry g = getAgent().getObject().getGeom();
		if(g == null) LOGGER.warn("Null geometry found for "+getAgent().getId());
		currentArea = g==null? 0 : g.getArea();
	}

	@Override
	public void computeGoalValue() {
		AFace af = getAgent();
		double msf = af.isHole()? minSizeDelHole : minSizeDel;
		goalArea = initialArea<msf? 0 : initialArea<minSize ? minSize : initialArea;
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

		AFace af = getAgent();
		Face f = af.getObject();

		boolean deletionAllowed = deletionAllowed();

		//deletion case
		if(goalArea == 0 && deletionAllowed ) {
			if(f.isIsland())
				//propose deletion
				out.add(new TFaceIslandDeletion(af));
			else
				//propose aggregation
				out.add(new TFaceAggregation(af));
			return out;
		}

		//face size should be changed to goalSize. Try first scaling.
		if(!af.hasFrozenEdge()) {
			if(f.isIsland() || f.isEnclave()) {
				for(double k : new double[]{1, 0.8, 0.5, 0.1})
					out.add(new TFaceScaling(af, k*Math.sqrt(goalArea/currentArea)));
			} else {
				//TODO scaling/deformation for non islands and non enclave
			}
		}
		//if face size is still too small, delete it
		if(goalArea < minSize && deletionAllowed) {
			if(f.isIsland())
				out.add(new TFaceIslandDeletion(af));
			else
				out.add(new TFaceAggregation(af));
		}
		return out;
	}


	//check if deletion is allowed
	private boolean deletionAllowed() {
		AFace af = getAgent();
		if(preserveAllUnits && af.lastUnitFace()) return false;
		if(preserveIfPointsInIt && af.points != null && af.points.size()>0) return false;
		return true;
	}

}
