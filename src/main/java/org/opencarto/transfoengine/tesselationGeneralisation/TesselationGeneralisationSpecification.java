/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.transfoengine.CartographicResolution;
import org.opencarto.util.ProjectionUtil.CRSType;

/**
 * @author julien Gaffuri
 *
 */
public class TesselationGeneralisationSpecification {
	CartographicResolution res;

	private boolean removeNarrowGaps, removeNarrowParts, preserveAllUnits, preserveIfPointsInIt, noTriangle;
	private int quad;

	private double nodingResolution;
	public double getNodingResolution() { return nodingResolution; }

	public TesselationGeneralisationSpecification(CartographicResolution res, CRSType crsType) { this(res, crsType, true, false, true, true, true, 4); }
	public TesselationGeneralisationSpecification(CartographicResolution res, CRSType crsType, boolean removeNarrowGaps, boolean removeNarrowParts, boolean preserveAllUnits, boolean preserveIfPointsInIt, boolean noTriangle, int quad) {
		this.res=res;
		this.removeNarrowGaps = removeNarrowGaps;
		this.removeNarrowParts = removeNarrowParts;
		this.preserveAllUnits = preserveAllUnits;
		this.preserveIfPointsInIt = preserveIfPointsInIt;
		this.noTriangle = noTriangle;
		nodingResolution = crsType==CRSType.CARTO?1e-5:1e-8;
		this.quad = quad;
	}

	public void setUnitConstraints(ATesselation t) {
		for(AUnit a : t.aUnits) {
			if(removeNarrowGaps) a.addConstraint(new CUnitNoNarrowGaps(a, res.getSeparationDistanceMeter(), getNodingResolution(), quad, preserveAllUnits, preserveIfPointsInIt).setPriority(10));
			if(removeNarrowParts) a.addConstraint(new CUnitNoNarrowParts(a, res.getSeparationDistanceMeter(), getNodingResolution(), quad, preserveAllUnits, preserveIfPointsInIt).setPriority(9));
			if(preserveIfPointsInIt) a.addConstraint(new CUnitContainPoints(a));
			if(noTriangle) a.addConstraint(new CUnitNoTriangle(a));
		}
	}
	public void setTopologicalConstraints(ATesselation t) {
		for(AFace a : t.aFaces) {
			a.addConstraint(new CFaceSize(a, 0.1*res.getPerceptionSizeSqMeter(), 3*res.getPerceptionSizeSqMeter(), res.getPerceptionSizeSqMeter(), preserveAllUnits, preserveIfPointsInIt).setPriority(2));
			a.addConstraint(new CFaceValidity(a));
			if(preserveIfPointsInIt) a.addConstraint(new CFaceContainPoints(a));
			if(noTriangle) a.addConstraint(new CFaceNoTriangle(a));
		}
		for(AEdge a : t.aEdges) {
			a.addConstraint(new CEdgeGranularity(a, 2*res.getResolutionM()));
			a.addConstraint(new CEdgeValidity(a));
			if(noTriangle) a.addConstraint(new CEdgeNoTriangle(a));
			a.addConstraint(new CEdgeFaceSize(a).setImportance(6));
			if(preserveIfPointsInIt) a.addConstraint(new CEdgesFacesContainPoints(a));
		}
	}
}
