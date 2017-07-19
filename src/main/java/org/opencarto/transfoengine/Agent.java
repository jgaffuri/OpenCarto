/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author julien Gaffuri
 *
 */
public class Agent {

	public Agent(Object object){
		this.object=object;
	}

	private Object object;
	public Object getObject() { return object; }

	private Collection<Constraint> constraints = new HashSet<Constraint>();
	public boolean addConstraint(Constraint c) { return constraints.add(c); }
	public boolean removeConstraint(Constraint c) { return constraints.remove(c); }
	public void clearConstraints() { constraints.clear(); }

	//from 0 to 10 (satisfied)
	protected double satisfaction;
	public double getSatisfaction() { return satisfaction; }

	//by default, the average of the satisfactions of the soft constraints. 0 if any hard constraint is unsatisfied.
	public void computeSatisfaction() {
		if(isDeleted() || constraints.size()==0) { satisfaction=10; return; }
		satisfaction=0; int nb=0;
		for(Constraint c : constraints){
			c.computeCurrentValue();
			c.computeGoalValue();
			c.computeSatisfaction();
			if(c.isHard() && c.getSatisfaction()<10) {
				satisfaction = 0;
				return;
			}
			if(c.isHard()) continue;
			satisfaction += c.getSatisfaction();
			nb++;
		}
		satisfaction /= nb ;
	}


	private boolean deleted = false;
	public boolean isDeleted() { return deleted; }
	public void setDeleted(boolean deleted) { this.deleted = deleted; }


}
