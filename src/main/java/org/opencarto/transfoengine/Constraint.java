/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.Comparator;
import java.util.List;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Constraint implements Comparable<Constraint>{

	//the object the constraint relates to
	private Agent agent;
	public Agent getAgent() { return agent; }

	public Constraint(Agent agent){
		this.agent = agent;
	}


	//a constraint whose satisfaction is expected to be 0 or 10, which has to be satisfied. Example: a topological constraint.
	public boolean isHard() { return false; }

	//importance (used for soft constraints only, to compute agent's overall satisfaction).
	double importance = 1;
	public double getImportance() { return importance; }
	public Constraint setImportance(double importance) { this.importance = importance; return this; }

	//from 0 to 10 (satisfied)
	protected double satisfaction = 10;
	public double getSatisfaction() { return satisfaction; }

	public abstract void computeCurrentValue();
	public abstract void computeGoalValue();
	public abstract void computeSatisfaction();

	//used to determine which constraints' should be satisfied inpriority
	double priority = 1;
	public double getPriority() { return priority; }
	public Constraint setPriority(double priority) { this.priority = priority; return this; }

	public abstract List<Transformation> getTransformations();



	
	public int compareTo(Constraint c) {
		return (int)(100000*(c.getPriority()-this.getPriority()));
	}

	public static final Comparator<Constraint> COMPARATOR_CONSTR = new Comparator<Constraint>(){
		public int compare(Constraint c0, Constraint c1) {
			return c0.compareTo(c1);
		}};
	
}
