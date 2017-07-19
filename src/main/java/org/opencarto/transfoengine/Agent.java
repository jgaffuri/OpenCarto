/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.opencarto.io.CSVUtil;

/**
 * @author julien Gaffuri
 *
 */
public class Agent {
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

	private Collection<Constraint> constraints = new HashSet<Constraint>();
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


	private boolean deleted = false;
	public boolean isDeleted() { return deleted; }
	public void setDeleted(boolean deleted) { this.deleted = deleted; }




	public static void saveStateReport(Collection<Agent> agents, String outPath, String outFile){
		ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
		for(Agent ag : agents){
			HashMap<String, String> d = new HashMap<String, String>();
			ag.computeSatisfaction();
			d.put("id", ag.id);
			for(Constraint c:ag.constraints)
				d.put(c.getClass().getSimpleName(), ""+c.getSatisfaction());
			d.put("satisfaction", ""+ag.getSatisfaction());
			data.add(d);
		}
		CSVUtil.save(data, outPath, outFile);


		/*new File(outPath).mkdirs();
		File f = new File(outPath+outFile);
		if(f.exists()) f.delete();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.write("id,satisfaction\n");
			for(Agent ag : agents){
				ag.computeSatisfaction();
				bw.write(ag.id+","+ag.getSatisfaction()+"\n");
			}
			bw.close();
		} catch (Exception e) { e.printStackTrace(); }*/


	}
}
