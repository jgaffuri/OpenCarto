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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
	private final static Logger LOGGER = Logger.getLogger(CUnitNoding.class.getName());

	private SpatialIndex index;
	private TopologyException nodingException = null;

	public CUnitNoding(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitNoding "+getAgent().getObject().id);

		ArrayList<Geometry> lineCol = new ArrayList<Geometry>();
		Geometry geom = getAgent().getObject().getGeom();
		for(Feature au : (List<Feature>) index.query(geom.getEnvelopeInternal()))
			lineCol.add(au.getGeom().getBoundary());

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
