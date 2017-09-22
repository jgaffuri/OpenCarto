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

import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * Constraint ensuring that a unit has no narrow gap.
 * Gaps are detected on-the-fly. It is assumed that a narrow part is the complementary
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoNarrowGaps extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowGaps.class);

	private double resolution, sizeDel; int quad;
	public CUnitNoNarrowGaps(AUnit agent, double resolution, double sizeDel, int quad) {
		super(agent);
		this.resolution = resolution;
		this.sizeDel = sizeDel;
		this.quad = quad;
	}

	private Collection<Polygon> gaps = null;

	@Override
	public void computeCurrentValue() {
		String mess = null;
		for(double k : new double[]{1.0, 0.9999, 0.999, 1.0001, 1.001}){
			try {
				gaps = MorphologicalAnalysis.getNarrowGaps(getAgent().getObject().getGeom(), k*resolution, sizeDel, quad);
				return;
			} catch (Exception e) { mess = e.getMessage(); }
		}
		LOGGER.warn("Could not compute narrow gaps for unit "+getAgent().getId()+". Message: "+mess);
	}

	@Override
	public void computeSatisfaction() {
		if(gaps==null) { /*satisfaction = 10;*/ return; }
		//compute total gaps area
		double tA=0; for(Polygon gap : gaps) tA+=gap.getArea();
		double a = getAgent().getObject().getGeom().getArea();
		satisfaction = 10.0*(1.0 - tA/a);
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
					au.absorbGaps(gaps, true, true);
				} catch (Exception e) {
					System.err.println("Failed absorbing straits for "+au.getId() + "  "+e.getMessage());
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
