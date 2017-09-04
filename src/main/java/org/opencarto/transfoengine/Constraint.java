/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Constraint implements Comparable<Constraint>{
	public final static Logger LOGGER = Logger.getLogger(Constraint.class.getName());

	//the object the constraint relates to
	private Agent agent;
	public Agent getAgent() { return agent; }

	public Constraint(Agent agent){
		this.agent = agent;
		computeInitialValue();
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
	public boolean isSatisfied(double satisfactionResolution) { return 10-this.getSatisfaction() < satisfactionResolution; }

	public void computeInitialValue() {}
	public abstract void computeCurrentValue();
	public void computeGoalValue() {}
	public abstract void computeSatisfaction();

	//used to determine which constraints' should be satisfied in priority
	double priority = 1;
	public double getPriority() { return priority; }
	public Constraint setPriority(double priority) { this.priority = priority; return this; }

	public abstract List<Transformation<?>> getTransformations();



	public String getMessage(){
		return new StringBuffer()
				.append(getAgent().getClass().getSimpleName()).append(",")
				.append(getAgent().getId()).append(",")
				.append(getClass().getSimpleName()).append(",")
				.append("pri=").append(getPriority()).append(",")
				.append("imp=").append(getImportance()).append(",")
				.append("s=").append(getSatisfaction())
				//TODO include constraint's position?
				.toString();
	}


	public int compareTo(Constraint c) {
		return (int)(100000*(c.getPriority()-this.getPriority()));
	}

	public static final Comparator<Constraint> COMPARATOR_CONSTR = new Comparator<Constraint>(){
		public int compare(Constraint c0, Constraint c1) {
			return c0.compareTo(c1);
		}
	};

	public static final Comparator<Constraint> COMPARATOR_CONSTR_BY_SATISFACTION = new Comparator<Constraint>(){
		public int compare(Constraint c0, Constraint c1) {
			return (int)(100000000*(c1.getSatisfaction()-c0.getSatisfaction()));
		}
	};

}
