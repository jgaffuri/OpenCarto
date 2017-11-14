/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

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

	List<Intersection> inters;
	SpatialIndex index;

	public CUnitNoOverlap(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		inters = new ArrayList<Intersection>();
		//TODO retrieve all units overlapping, with spatial index
		MultiPolygon geom = (MultiPolygon) getAgent().getObject().getGeom();
		for(Feature unit : (List<Feature>)index.query(geom.getEnvelopeInternal())) {
			if(unit == getAgent().getObject()) continue;
			if(!geom.getEnvelopeInternal().intersects(unit.getGeom().getEnvelopeInternal())) continue;

			//compute intersection area
			//TODO use overlap first?
			double interArea = geom.intersection(unit.getGeom()).getArea();
			if(interArea == 0) continue;

			inters.add(new Intersection(unit.id, interArea, interArea/geom.getArea()));
		}
		//TODO sort inters ?
	}

	@Override
	public void computeSatisfaction() {
		if(inters.size()!=0) System.out.println(getAgent().getObject().id + " " + inters.size());
		if(inters == null || inters.size()==0) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
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
