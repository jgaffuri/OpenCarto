/**
 * 
 */
package org.opencarto.transfoengine;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Transformation<T extends Agent> {
	//private final static Logger LOGGER = Logger.getLogger(Transformation.class);

	private T agent;
	public T getAgent() { return agent; }

	public Transformation(T agent){
		this.agent = agent;
	}

	public abstract void apply();	

	public abstract boolean isCancelable();	

	public String toString(){ return getClass().getSimpleName(); }

}
