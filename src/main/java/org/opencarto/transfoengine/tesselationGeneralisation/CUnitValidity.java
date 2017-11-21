/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

/**
 * @author julien Gaffuri
 *
 */
public class CUnitValidity  extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitValidity.class);

	TopologyValidationError error = null;

	public CUnitValidity(AUnit agent) { super(agent); }

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitValid "+getAgent().getObject().id);
		//valid = getAgent().getObject().getGeom().isValid();
		IsValidOp ivo = new IsValidOp( getAgent().getObject().getGeom() );
		error = ivo.getValidationError();
		System.out.println(this.getMessage());
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = error==null? 10 : 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		return new ArrayList<Transformation<AUnit>>();
	}

	@Override
	public String getMessage(){
		return super.getMessage() + ( error!=null? ("," + error.getCoordinate().toString().replaceAll(",", ";") + "," + error.getMessage()) : ",no error" );
	}
}
