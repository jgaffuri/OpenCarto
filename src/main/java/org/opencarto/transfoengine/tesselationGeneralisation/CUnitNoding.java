/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.locationtech.jts.index.SpatialIndex;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssue;
import org.opencarto.algo.noding.NodingUtil.NodingIssueType;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * 
 * Check that a unit is correctly noded to its touching ones.
 * 
 * @author julien
 *
 */
public class CUnitNoding  extends Constraint<AUnit> {
	public final static Logger LOGGER = Logger.getLogger(CUnitNoding.class.getName());

	private SpatialIndex index;
	private NodingIssueType nType;
	private double res;
	private Collection<NodingIssue> nis = null;
	public Collection<NodingIssue> getIssues() { return nis; }

	public CUnitNoding(AUnit agent, SpatialIndex index, NodingIssueType nType, double nodingResolution) {
		super(agent);
		this.index = index;
		this.nType = nType;
		this.res = nodingResolution;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.debug("CUnitNoding "+getAgent().getObject().id);
		nis = NodingUtil.getNodingIssues(nType, getAgent().getObject(), index, res);
	}

	@Override
	public void computeSatisfaction() {
		//TODO better? progressive depending on number of issues related to number of vertices?
		if(nis == null || nis.size()==0) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		//TODO propose noding fixing algorithm?
		return new ArrayList<Transformation<AUnit>>();
	}

	public String getMessage(){
		StringBuffer sb = new StringBuffer(super.getMessage());
		if(nis != null)
			for(NodingIssue ni : nis)
				sb.append(",").append(ni.type).append(",").append(ni.c.toString()).append(",").append(ni.distance);
		return sb.toString();
	}

}
