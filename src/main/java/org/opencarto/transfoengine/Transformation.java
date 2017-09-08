/**
 * 
 */
package org.opencarto.transfoengine;

import org.apache.log4j.Logger;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Transformation<T extends Agent> {
	public final static Logger LOGGER = Logger.getLogger(Transformation.class);

	protected T agent;

	public Transformation(T agent){
		this.agent = agent;
	}

	public abstract void apply();	

	public abstract boolean isCancelable();	
	public abstract void storeState();	
	public abstract void cancel();	


	public String toString(){
		return getClass().getSimpleName();
	}

}
