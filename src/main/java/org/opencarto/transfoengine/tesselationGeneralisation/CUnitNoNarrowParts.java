/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * Constraint ensuring that a unit has no narrow part.
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoNarrowParts extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowParts.class.getName());

	private double widthMeter, nodingDistance; private int quad; boolean preserveAllUnits;
	public CUnitNoNarrowParts(AUnit agent, double widthMeter, double nodingDistance, int quad, boolean preserveAllUnits) {
		super(agent);
		this.widthMeter = widthMeter;
		this.nodingDistance = nodingDistance;
		this.quad = quad;
		this.preserveAllUnits = preserveAllUnits;
	}

	//the narrow parts
	private Collection<Polygon> nps;

	@Override
	public void computeCurrentValue() {
		//compute narrow parts
		nps = MorphologicalAnalysis.getNarrowParts(getAgent().getObject().getGeom(), widthMeter, quad);
	}

	@Override
	public void computeSatisfaction() {
		//depends on the size of the narrow parts
		double a = getAgent().getObject().getGeom().getArea();
		if(a==0) { satisfaction = 10; return; }
		double snpa=0; for(Polygon np : nps) snpa += np.getArea();
		satisfaction = 10*(1-snpa/a);
		satisfaction = satisfaction>10? 10 : satisfaction<0? 0 : satisfaction;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		ArrayList<Transformation<AUnit>> out = new ArrayList<Transformation<AUnit>>();
		out.add(new T(getAgent()));
		return out;
	}

	private class T extends Transformation<AUnit> {

		public T(AUnit agent) { super(agent); }

		@Override
		public void apply() {
			Feature unit = getAgent().getObject();
			ATesselation t = getAgent().getAtesselation();
			for(Polygon np : nps) {
				np = (Polygon) np.buffer(widthMeter*0.001, quad);
				Geometry newUnitGeom = null;
				try {
					newUnitGeom = unit.getGeom().union(np);
				} catch (Exception e1) {
					LOGGER.warn("Could not make difference of unit "+unit.id+" with narrow part around " + np.getCentroid().getCoordinate() + " Exception: "+e1.getClass().getName());
					continue;
				}
				//set new geometry
				unit.setGeom(JTSGeomUtil.toMulti(newUnitGeom));
			}
			//TODO check point thing
			//TODO rebuild noding in the end
		}

		//TODO make it cancellable - with geometry storage?
		@Override
		public boolean isCancelable() { return false; }
	}

}
