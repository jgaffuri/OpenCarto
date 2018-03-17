/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;

/**
 * Ensure the edge face constraint (if any) is satisfied:
 * 
 * @author julien Gaffuri
 *
 */
public class CEdgeFaceSize extends Constraint<AEdge> {
	private final static Logger LOGGER = Logger.getLogger(CEdgeFaceSize.class.getName());

	private CFaceSize sc1=null, sc2=null;

	public CEdgeFaceSize(AEdge agent) {
		super(agent);
		for(Face f : agent.getObject().getFaces()){
			AFace af = agent.getAtesselation().getAFace(f);
			CFaceSize sc = (CFaceSize) af.getConstraint(CFaceSize.class);
			if(sc==null) continue;
			if(sc1==null) sc1=sc;
			else if(sc2==null) sc2=sc;
			else LOGGER.error("Unexpected number of faces found when creating CEdgeFaceSize.");
		}
	}

	@Override
	public void computeCurrentValue() {
		if(sc1!=null) sc1.computeCurrentValue();
		if(sc2!=null) sc2.computeCurrentValue();
	}


	@Override
	public void computeSatisfaction() {
		int nbS=0;
		double s = 0;
		if(sc1!=null) {sc1.computeSatisfaction(); s+=sc1.getSatisfaction(); nbS++;}
		if(sc2!=null) {sc2.computeSatisfaction(); s+=sc2.getSatisfaction(); nbS++;}
		satisfaction = nbS>0? s/nbS : 10;
	}

}
