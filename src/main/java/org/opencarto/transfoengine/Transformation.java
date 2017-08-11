/**
 * 
 */
package org.opencarto.transfoengine;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Transformation<T extends Agent> {
	protected T agent;

	public Transformation(T agent){
		this.agent = agent;
	}

	public abstract void storeState();	
	public abstract void apply();	
	public abstract void cancel();	

}
