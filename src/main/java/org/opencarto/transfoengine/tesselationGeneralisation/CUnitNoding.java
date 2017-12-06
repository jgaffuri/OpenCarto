/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.index.SpatialIndex;

/**
 * 
 * Check a unit is correctly noded to its touching ones.
 * 
 * @author julien
 *
 */
public class CUnitNoding  extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoding.class);

	SpatialIndex index;
	Object nodingIssue = null;

	public CUnitNoding(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitNoding "+getAgent().getObject().id);

		/*/retrieve all units overlapping, with spatial index
		MultiPolygon geom = (MultiPolygon) getAgent().getObject().getGeom();
		for(Feature unit : (List<Feature>)index.query(geom.getEnvelopeInternal())) {
			if(unit == getAgent().getObject()) continue;
			if(!geom.getEnvelopeInternal().intersects(unit.getGeom().getEnvelopeInternal())) continue;

			//check overlap
			boolean overlap = false;
			try {
				overlap = geom.overlaps(unit.getGeom());
			} catch (Exception e) {
				//overlaps.add(new Overlap(unit.id, null, -1, -1));
				continue;
			}
			if(!overlap) continue;

			Geometry inter = geom.intersection(unit.getGeom());
			double interArea = inter.getArea();
			if(interArea == 0) continue;
			//overlaps.add(new Overlap(unit.id, inter.getCoordinate(), interArea, 100.0*interArea/geom.getArea()));
		}*/
	}

	@Override
	public void computeSatisfaction() {
		if(nodingIssue == null) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
	}

	public String getMessage(){
		StringBuffer sb = new StringBuffer(super.getMessage());
		sb.append(",").append(nodingIssue);
		return sb.toString();
	}

}
