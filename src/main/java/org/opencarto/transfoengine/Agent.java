/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.opencarto.io.CSVUtil;
import org.opencarto.util.Util;

/**
 * @author julien Gaffuri
 *
 */
public abstract class Agent {
	//public final static Logger LOGGER = Logger.getLogger(Agent.class.getName());

	private static int ID_COUNT=1;	
	private String id;
	public String getId() { return id; }
	public Agent setId(String id) { this.id = id; return this; }

	public Agent(Object object){
		this.object=object;
		id="ag"+(ID_COUNT++);
	}

	private Object object;
	public Object getObject() { return object; }

	private List<Constraint> constraints = new ArrayList<Constraint>();
	public boolean addConstraint(Constraint c) { return constraints.add(c); }
	public boolean removeConstraint(Constraint c) { return constraints.remove(c); }
	public void clearConstraints() { constraints.clear(); }

	//from 0 to 10 (satisfied)
	protected double satisfaction = 10;
	public double getSatisfaction() { return satisfaction; }

	//by default, the average of the satisfactions of the soft constraints. 0 if any hard constraint is unsatisfied.
	public void computeSatisfaction() {
		if(isDeleted() || constraints.size()==0) { satisfaction=10; return; }
		satisfaction=0; double sImp=0;
		for(Constraint c : constraints){
			c.computeCurrentValue();
			c.computeGoalValue();
			c.computeSatisfaction();
			if(c.isHard() && c.getSatisfaction()<10) {
				satisfaction = 0;
				return;
			}
			if(c.isHard()) continue;
			satisfaction += c.getImportance() * c.getSatisfaction();
			sImp += c.getImportance();
		}
		if(sImp==0) satisfaction = 10; else satisfaction /= sImp ;
	}

	//flag to mark that the agent is deleted
	private boolean deleted = false;
	public boolean isDeleted() { return deleted; }
	public void setDeleted(boolean deleted) { this.deleted = deleted; }


	//retrieve list of candidate transformations to try improving agent's satisfaction
	public List<Transformation<?>> getTransformations(){
		List<Transformation<?>> tr = new ArrayList<Transformation<?>>();
		if(isDeleted()) return tr;

		constraints.sort(Constraint.COMPARATOR_CONSTR);
		for(Constraint c : constraints) {
			if(c.getSatisfaction()==10) continue;
			tr.addAll(c.getTransformations());
		}
		return tr;
	}




	//produce and save a report on agents' states
	public static void saveStateReport(Collection<?> agents, String outPath, String outFile){
		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
		for(Object ag_ : agents){
			Agent ag = (Agent)ag_;
			HashMap<String, String> d = new HashMap<String, String>();
			ag.computeSatisfaction();
			d.put("id", ag.id);
			for(Constraint c:ag.constraints)
				d.put(c.getClass().getSimpleName(), ""+c.getSatisfaction());
			d.put("satisfaction", ""+ag.getSatisfaction());
			data.add(d);
		}
		CSVUtil.save(data, outPath, outFile);
	}


	//lifecycle of the agent
	public void activate() {
		//LOGGER.fine("Activate agent: "+toString());

		//compute satisfaction
		this.computeSatisfaction();
		double sat1 = this.getSatisfaction();

		//satisfaction perfect: nothing to do.
		if(sat1 == 10) return;

		//get list of candidate transformations from agent
		List<Transformation<?>> ts = this.getTransformations();
		while(ts.size()>0){
			Transformation<?> t = ts.get(0);
			ts.remove(0);

			//save current state
			if(t.isCancelable()) t.storeState();

			//apply transformation
			//LOGGER.fine("Apply "+t.toString());
			//System.out.println("Apply "+t.toString()+" to "+this.toString() + " "+((Face)this.getObject()).isEnclave());
			t.apply();

			//get new satisfaction
			this.computeSatisfaction();
			double sat2 = this.getSatisfaction();

			if(sat2 == 10) {
				//perfect state reached: end
				return;
			} else if(sat2 - sat1 > 0){
				//improvement: get new list of candidate transformations
				ts = this.getTransformations();
				sat1 = sat2;
			} else {
				//no improvement: go back to previous state, if possible
				if(t.isCancelable())
					t.cancel();
				else if(sat2 - sat1 < 0)
					System.err.println("Non cancellable transformation "+t.getClass().getSimpleName()+" resulted in satisfaction decrease for agent "+this.getId());
			}
		}
	}


	public String toString(){
		return getClass().getSimpleName()+"-"+getId()+" (satisf="+Util.round(satisfaction,3)+",nbContr="+constraints.size()+",obj="+getObject().toString()+")";
	}

}
