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
	private Agent agent;
	public Agent getAgent() { return agent; }

	public Constraint(Agent agent){
		this.agent = agent;
	}

	//from 0 to 10 (satisfied)
	protected double satisfaction;
	public double getSatisfaction() { return satisfaction; }

	public abstract void computeCurrentValue();
	public abstract void computeGoalValue();
	public abstract void computeSatisfaction();

}
