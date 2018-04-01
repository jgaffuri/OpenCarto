/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.algo.noding.NodingUtil.NodingIssueType;
import org.opencarto.algo.polygon.MorphologicalAnalysis;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * Constraint ensuring that a unit has no narrow gap.
 * 
 * @author julien Gaffuri
 *
 */
public class CUnitNoNarrowGaps extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoNarrowGaps.class.getName());

	private double separationDistanceMeter, nodingResolution; private int quad; private boolean preserveAllUnits, preserveIfPointsInIt;
	public CUnitNoNarrowGaps(AUnit agent, double separationDistanceMeter, double nodingResolution, int quad, boolean preserveAllUnits, boolean preserveIfPointsInIt) {
		super(agent);
		this.separationDistanceMeter = separationDistanceMeter;
		this.nodingResolution = nodingResolution;
		this.quad = quad;
		this.preserveAllUnits = preserveAllUnits;
		this.preserveIfPointsInIt = preserveIfPointsInIt;
	}

	//the narrow gaps
	private Collection<Polygon> ngs;

	@Override
	public void computeCurrentValue() {
		//compute narrow gaps
		ngs = MorphologicalAnalysis.getNarrowGaps(getAgent().getObject().getGeom(), separationDistanceMeter, quad);
	}

	@Override
	public void computeSatisfaction() {
		//depends on the size of the narrow gaps
		double a = getAgent().getObject().getGeom().getArea();
		if(a==0) { satisfaction = 10; return; }
		double snga=0; for(Polygon ng : ngs) snga += ng.getArea();
		satisfaction = 10*(1-snga/a);
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
			Collection<Feature> unitsNoding = new ArrayList<Feature>();
			unitsNoding.add(getAgent().getObject());

			Feature unit = getAgent().getObject();
			ATesselation t = getAgent().getAtesselation();
			for(Polygon ng : ngs) {
				ng = (Polygon) ng.buffer(separationDistanceMeter*0.001, quad);
				Geometry newUnitGeom = null;
				try {
					newUnitGeom = unit.getGeom().union(ng);
				} catch (Exception e1) {
					LOGGER.warn("Could not make union of unit "+unit.id+" with gap around " + ng.getCentroid().getCoordinate() + " Exception: "+e1.getClass().getName());
					continue;
				}

				//get units intersecting and correct their geometries
				Collection<AUnit> uis = t.query( ng.getEnvelopeInternal() );
				for(AUnit aui : uis) {
					Feature ui = aui.getObject();
					if(ui == unit) continue;
					if(!ui.getGeom().getEnvelopeInternal().intersects(ng.getEnvelopeInternal())) continue;

					//compute the candidate geometry: the difference
					Geometry geomC = ui.getGeom().difference(ng);

					//check not the whole unit has disappear
					if(preserveAllUnits && (geomC==null || geomC.isEmpty())) {
						LOGGER.trace("Unit "+ui.id+" disappeared when removing gaps of unit "+unit.id+" around "+ng.getCentroid().getCoordinate());
						newUnitGeom = newUnitGeom.difference(ui.getGeom());
						continue;
					}

					//check if point has left it
					Geometry geomS = ui.getGeom();
					ui.setGeom(geomC);
					if(preserveIfPointsInIt && !getAgent().getAtesselation().getAUnit(ui).containPoints()) {
						LOGGER.trace("Unit "+ui.id+" has lost some point in it when removing gaps of unit "+unit.id+" around "+ng.getCentroid().getCoordinate());
						ui.setGeom(geomS);
						newUnitGeom = newUnitGeom.difference(ui.getGeom());
						continue;
					}

					unitsNoding.add(ui);

					//set new geometry
					ui.setGeom(JTSGeomUtil.toMulti(geomS));
				}

				//set new geometry
				unit.setGeom(JTSGeomUtil.toMulti(newUnitGeom));
			}

			LOGGER.trace("Ensure noding");
			NodingUtil.fixNoding(NodingIssueType.PointPoint, unitsNoding, nodingResolution);
			NodingUtil.fixNoding(NodingIssueType.LinePoint, unitsNoding, nodingResolution);
		}

		//TODO make it cancellable - with geometry storage?
		@Override
		public boolean isCancelable() { return false; }
	}

}
