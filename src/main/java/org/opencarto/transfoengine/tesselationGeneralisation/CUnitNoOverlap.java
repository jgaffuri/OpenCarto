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

	List<Intersection> inters;
	SpatialIndex index;

	public CUnitNoOverlap(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info(getAgent().getObject().id);

		inters = new ArrayList<Intersection>();
		//TODO retrieve all units overlapping, with spatial index
		MultiPolygon geom = (MultiPolygon) getAgent().getObject().getGeom();
		for(Feature unit : (List<Feature>)index.query(geom.getEnvelopeInternal())) {
			if(unit == getAgent().getObject()) continue;
			if(!geom.getEnvelopeInternal().intersects(unit.getGeom().getEnvelopeInternal())) continue;

			//compute intersection area
			//TODO use overlap first?
			double interArea = 0;
			try {
				interArea = geom.intersection(unit.getGeom()).getArea();
			} catch (Exception e) {
				inters.add(new Intersection(unit.id, -1, -1));
			}

			if(interArea == 0) continue;
			inters.add(new Intersection(unit.id, interArea, 100.0*interArea/geom.getArea()));
		}
		//TODO sort inters ?
	}

	@Override
	public void computeSatisfaction() {
		//if(inters.size()!=0) System.out.println(getAgent().getObject().id + " " + inters.size());
		if(inters == null || inters.size()==0) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
	}

	public String getMessage(){
		StringBuffer sb = new StringBuffer(super.getMessage());
		for(Intersection inter : inters)
			sb.append(",").append(inter.id).append(",").append(inter.area).append(",").append(inter.percentage).append("%");
		return sb.toString();
	}


	public class Intersection {
		public Intersection(String id, double area, double percentage) {
			this.id = id;
			this.area = area;
			this.percentage = percentage;
		}
		String id;
		double area;
		double percentage;
	}

}
