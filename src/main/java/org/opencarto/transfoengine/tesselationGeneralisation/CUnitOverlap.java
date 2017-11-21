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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
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
				//overlaps.add(new Overlap(unit.id, null, -1, -1));
				continue;
			}
			if(!overlap) continue;

			Geometry inter = geom.intersection(unit.getGeom());
			double interArea = inter.getArea();
			if(interArea == 0) continue;
			overlaps.add(new Overlap(unit.id, inter.getCoordinate(), interArea, 100.0*interArea/geom.getArea()));
		}
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
		for(Overlap overlap : overlaps)
			sb.append(",").append(overlap.id).append(",").append(overlap.position.toString().replace(",", ";")).append(",").append(overlap.area).append(",").append(overlap.percentage).append("%");
		return sb.toString();
	}


	public class Overlap {
		public Overlap(String id, Coordinate position, double area, double percentage) {
			this.id = id;
			this.position = position;
			this.area = area;
			this.percentage = percentage;
		}
		String id;
		Coordinate position;
		double area;
		double percentage;
	}

}
