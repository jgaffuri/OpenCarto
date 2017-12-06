/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
	private final static Logger LOGGER = Logger.getLogger(CUnitNoding.class);

	SpatialIndex index;
	TopologyException nodingException = null;

	public CUnitNoding(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitNoding "+getAgent().getObject().id);

		Collection<Geometry> lineCol = new HashSet<Geometry>();
		for(Feature unit : (List<Feature>)index.query(getAgent().getObject().getGeom().getEnvelopeInternal())) {
			if(unit == getAgent().getObject()) continue;
			//if(!geom.getEnvelopeInternal().intersects(unit.getGeom().getEnvelopeInternal())) continue;

			System.out.println(unit.id);

			lineCol.add(unit.getGeom().getBoundary());
		}

		Geometry union = null;
		try {
			union = new GeometryFactory().buildGeometry(lineCol).union();
		} catch (TopologyException e) {
			nodingException = e;
		}
		//System.out.println(union.getCentroid());
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
		sb.append(",").append(nodingException);
		return sb.toString();
	}

}
