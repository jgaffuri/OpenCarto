/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;

/**
 * 
 * Constraint ensuring that a unit has no narrow gap.
 * Gaps are detected on-the-fly. It is assumed that a narrow part is the complementary
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoNarrowGap extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowGap.class);

	private double resolution, sizeDel; int quad;
	public CUnitNoNarrowGap(AUnit agent, double resolution, double sizeDel, int quad) {
		super(agent);
	}

	private Collection<Polygon> gaps;

	@Override
	public void computeCurrentValue() {
		gaps = MorphologicalAnalysis.getNarrowGaps(getAgent().getObject().getGeom(), resolution, sizeDel, quad);
	}

	@Override
	public void computeSatisfaction() {
		//compute total gaps area
		double tA=0; for(Polygon gap : gaps) tA+=gap.getArea();
		double a = getAgent().getObject().getGeom().getArea();
		satisfaction = 10*(1 - tA/a);
		if(satisfaction>10) satisfaction=10; else if(satisfaction<0) satisfaction=0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		ArrayList<Transformation<AUnit>> out = new ArrayList<Transformation<AUnit>>();
		//TODO popose union
		//compute union
		MultiPolygon union = null;
		try {
			Collection all = new ArrayList<Polygon>(); all.addAll(gaps); all.add(getAgent().getObject().getGeom());
			union = (MultiPolygon) CascadedPolygonUnion.union(all);
			gaps.clear();
		} catch (Exception e) {
			LOGGER.warn("Could not fill gaps with CascadedPolygonUnion for unit "+getAgent().getId()+". Message: "+e.getMessage());
		}

		return out;
	}

}
