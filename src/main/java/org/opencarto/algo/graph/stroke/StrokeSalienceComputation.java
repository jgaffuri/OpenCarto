/**
 * 
 */
package org.opencarto.algo.graph.stroke;

import java.util.Collection;

import org.apache.log4j.Logger;

/**
 * @author julien Gaffuri
 *
 */
public class StrokeSalienceComputation {
	public final static Logger LOGGER = Logger.getLogger(StrokeSalienceComputation.class.getName());

	//between 0 (not salient) and 1 (very salient)
	public double getSalience(Stroke s) {
		//TODO should be based on length and attribute value
		double sal = s.getGeom().getLength();
		return sal;
	};


	public void setSalience(Collection<Stroke> sts) {
		for(Stroke s : sts)
			s.set("sal", getSalience(s));
	}

}
