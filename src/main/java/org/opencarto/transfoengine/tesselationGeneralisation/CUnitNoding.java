/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssue;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * 
 * Check that a unit is correctly noded to its touching ones.
 * 
 * @author julien
 *
 */
public class CUnitNoding  extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoding.class.getName());

	private SpatialIndex index;
	private Collection<NodingIssue> nis = new HashSet<NodingIssue>();

	public CUnitNoding(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitNoding "+getAgent().getObject().id);

		Geometry geom = getAgent().getObject().getGeom();
		for(Feature au : (List<Feature>) index.query(geom.getEnvelopeInternal())) {
			if(au == getAgent().getObject()) continue;
			System.out.println(au.id);
			Collection<NodingIssue> nis_ = NodingUtil.analyseNoding(geom, au.getGeom());
			nis.addAll(nis_);
		}
	}

	@Override
	public void computeSatisfaction() {
		//TODO better?
		if(nis == null || nis.size()==0) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		//TODO make noding?
		return new ArrayList<Transformation<AUnit>>();
	}

	public String getMessage(){
		StringBuffer sb = new StringBuffer(super.getMessage());
		for(NodingIssue ni : nis)
			sb.append(",").append(ni.c.toString()); //.append(",").append(ni.distance);
		return sb.toString();
	}

}
