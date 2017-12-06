/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.algo.base.Union;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

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

		Collection<Geometry> lineCol = new HashSet<Geometry>();
		MultiPolygon geom = (MultiPolygon) getAgent().getObject().getGeom();
		for(Feature unit : (List<Feature>)index.query(geom.getEnvelopeInternal())) {
			if(unit == getAgent().getObject()) continue;
			if(!geom.getEnvelopeInternal().intersects(unit.getGeom().getEnvelopeInternal())) continue;
			lineCol.add(unit.getBoundary());
		}

		LOGGER.info("     compute union of boundaries...");
		//TODO find smarter ways to union lines?
		Geometry union = null;
		try {
			union = new GeometryFactory().buildGeometry(lineCol).union();
		} catch (TopologyException e1) {
			LOGGER.error("     Geometry.union failed. "+e1.getMessage());
			e1.printStackTrace();
			//TODO if error related to non noded geometries, node it and try again.
			union = Union.get(lineCol);
		}
		LOGGER.info("     linemerger...");
		LineMerger lm = new LineMerger();
		lm.add(union);
		union = null;
		Collection<LineString> lines = lm.getMergedLineStrings();
		lm = null;

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
