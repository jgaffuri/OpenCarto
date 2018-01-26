/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * 
 * Constraint ensuring that a unit has no narrow gap.
 * Gaps are detected on-the-fly. It is assumed that a narrow part is the complementary
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoNarrowGaps extends Constraint<AUnit> {
	//private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowGaps.class.getName());

	private double resolution, sizeDel; int quad;
	public CUnitNoNarrowGaps(AUnit agent, double resolution, double sizeDel, int quad) {
		super(agent);
		this.resolution = resolution;
		this.sizeDel = sizeDel;
		this.quad = quad;
	}

	private MultiPolygon filledGeom = null;

	@Override
	public void computeCurrentValue() {
		filledGeom = MorphologicalAnalysis.fillNarrowGaps(getAgent().getObject().getGeom(), resolution, sizeDel, quad);

		/*String mess = null;
		for(double k : new double[]{1.0, 0.9999, 0.999, 1.0001, 1.001}){
			try {
				gaps = MorphologicalAnalysis.getNarrowGaps(getAgent().getObject().getGeom(), k*resolution, sizeDel, quad);
				return;
			} catch (Exception e) { mess = e.getMessage(); }
		}
		LOGGER.warn("Could not compute narrow gaps for unit "+getAgent().getId()+". Message: "+mess);*/
	}

	@Override
	public void computeSatisfaction() {
		double a = getAgent().getObject().getGeom().getArea();
		double t = (filledGeom.getArea() - a)/a;
		satisfaction = 10*(1-t);
		/*if(gaps==null) return;
		//compute total gaps area
		double tA=0; for(Polygon gap : gaps) tA+=gap.getArea();
		//compute satisfaction
		double a = getAgent().getObject().getGeom().getArea();
		satisfaction = 10.0*(1.0 - tA/a);*/
		if(satisfaction>10) satisfaction=10; else if(satisfaction<0) satisfaction=0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		ArrayList<Transformation<AUnit>> out = new ArrayList<Transformation<AUnit>>();

		out.add(new Transformation<AUnit>((AUnit)getAgent()) {

			@Override
			public void apply() {
				AUnit au = getAgent();
				try {
					au.getObject().setGeom(filledGeom);
					//au.fillNarrowGaps(resolution, sizeDel, quad, true);
					//au.absorbGaps(gaps, true, true);
				} catch (Exception e) {
					System.err.println("Failed filing narrow straits for "+au.getId() + "  "+e.getMessage());
				}
			}

			@Override
			public boolean isCancelable() { return false; }
			@Override
			public void storeState() {}
			@Override
			public void cancel() { System.err.println("cancel() not implemented for "+this.getClass().getSimpleName()); }
			public String toString(){ return getClass().getSimpleName(); }
		});

		return out;
	}

}
