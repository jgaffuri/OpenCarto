/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * @author julien Gaffuri
 *
 */
public class CUnitNoOverlap  extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoOverlap.class);

	List<Overlap> overlaps;
	SpatialIndex index;

	public CUnitNoOverlap(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitNoOverlap "+getAgent().getObject().id);

		overlaps = new ArrayList<Overlap>();

		//retrieve all units overlapping, with spatial index
		MultiPolygon geom = (MultiPolygon) getAgent().getObject().getGeom();
		for(Feature unit : (List<Feature>)index.query(geom.getEnvelopeInternal())) {
			if(unit == getAgent().getObject()) continue;
			if(!geom.getEnvelopeInternal().intersects(unit.getGeom().getEnvelopeInternal())) continue;

			//check overlap
			boolean overlap = false;
			try {
				overlap = geom.overlaps(unit.getGeom());
			} catch (Exception e) {
				overlaps.add(new Overlap(unit.id, -1, -1));
				continue;
			}
			if(!overlap) continue;

			double interArea = geom.intersection(unit.getGeom()).getArea();
			if(interArea == 0) continue;
			overlaps.add(new Overlap(unit.id, interArea, 100.0*interArea/geom.getArea()));
		}
		//TODO sort inters ?
	}

	@Override
	public void computeSatisfaction() {
		//if(inters.size()!=0) System.out.println(getAgent().getObject().id + " " + inters.size());
		if(overlaps == null || overlaps.size()==0) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
	}

	public String getMessage(){
		StringBuffer sb = new StringBuffer(super.getMessage());
		for(Overlap inter : overlaps)
			sb.append(",").append(inter.id).append(",").append(inter.area).append(",").append(inter.percentage).append("%");
		return sb.toString();
	}


	public class Overlap {
		public Overlap(String id, double area, double percentage) {
			this.id = id;
			this.area = area;
			this.percentage = percentage;
		}
		String id;
		double area;
		double percentage;
	}

}
