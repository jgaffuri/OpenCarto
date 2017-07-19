/**
 * 
 */
package org.opencarto.transfoengine;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Constraint {

	//the object the constraint relates to
	private Object object;
	public Object getObject() { return object; }

	public Constraint(Object object){
		this.object = object;
	}

	//from 0 to 10 (satisfied)
	private double statisfaction;
	public double getStatisfaction() { return statisfaction; }

	protected abstract void computeCurrentValue();
	protected abstract void computeGoalValue();
	protected abstract void computeStatisfaction();

}
