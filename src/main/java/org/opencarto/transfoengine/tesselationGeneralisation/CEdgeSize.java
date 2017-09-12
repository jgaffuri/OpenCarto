/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.LineString;

/**
 * Ensure too short edges are deleted.
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeSize extends Constraint<AEdge> {
	double minimumSize, delSize;

	public CEdgeSize(AEdge agent, double minimumSize, double delSize) {
		super(agent);
		this.minimumSize = minimumSize;
		this.delSize = delSize;
	}

	double currentSize = -1;

	@Override
	public void computeCurrentValue() {
		currentSize = getAgent().getObject().getGeometry().getLength();
	}

	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted()) { satisfaction=10; return; }

		LineString g = getAgent().getObject().getGeometry();
		if(g.isClosed()) { satisfaction = 10; return; }
		if(currentSize > minimumSize) { satisfaction = 10; return; }

		satisfaction = 10 - 10*Math.abs(minimumSize-currentSize)/minimumSize;
		if(satisfaction<0) satisfaction=0;
	}

	@Override
	public List<Transformation<AEdge>> getTransformations() {
		ArrayList<Transformation<AEdge>> tr = new ArrayList<Transformation<AEdge>>();

		if(currentSize < delSize){
			//tr.add(new TEdgeCollapse((AEdge) getAgent())); //TODO ensure faces remain valid after edge collapse
		} else if(currentSize < minimumSize){
			//TODO add also edge lengthening?
		}

		return tr;
	}

}
