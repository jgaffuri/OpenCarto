/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.measure.unit.SystemOfUnits;

import org.apache.log4j.Logger;
import org.opencarto.algo.base.Union;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * 
 * Check a unit is correctly noded to its touching ones.
 * 
 * @author julien
 *
 */
public class CUnitNoding  extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoding.class);

	private SpatialIndex index;
	private TopologyException nodingException = null;

	public CUnitNoding(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitNoding "+getAgent().getObject().id);

		//get unit's boundaries
		Collection<MultiPolygon> mps = new HashSet<MultiPolygon>();
		Geometry geom = getAgent().getObject().getGeom().buffer(100000);
		Collection<Feature> aUnits = (List<Feature>) index.query(geom.getEnvelopeInternal());
		for(Feature au : aUnits)
			mps.add((MultiPolygon) au.getGeom());

		ArrayList<Geometry> lineCol = new ArrayList<Geometry>();
		for(MultiPolygon unit : mps) lineCol.add(unit.getBoundary());

		Geometry union = new GeometryFactory().buildGeometry(lineCol);
		try {
			union = union.union();
		} catch (TopologyException e) {
			nodingException = e;
		}

	}

	@Override
	public void computeSatisfaction() {
		if(nodingException == null) satisfaction = 10;
		else satisfaction = 0;
		System.out.println(satisfaction);
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
	}

	public String getMessage(){
		StringBuffer sb = new StringBuffer(super.getMessage());
		sb.append(",").append(nodingException.getCoordinate().toString().replace(",", ";")).append(",").append(nodingException);
		return sb.toString();
	}

}
