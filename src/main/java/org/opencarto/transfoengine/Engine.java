/**
 * 
 */
package org.opencarto.transfoengine;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

/**
 * @author julien Gaffuri
 *
 */
public class Engine<T extends Agent> {
	public final static Logger LOGGER = Logger.getLogger(Engine.class.getName());

	private ArrayList<T> agents;

	public Engine(Collection<T> agents, String logFilePath){
		this.agents = new ArrayList<T>();
		this.agents.addAll(agents);
		this.logFilePath = logFilePath;
	}


	//TODO implement/test other activation methods
	public void activateQueue(){
		for(Agent agent : agents)
			agent.activate(getLogWriter());
		closeLogger();
	}


	public void shuffle() {
		Collections.shuffle(agents);
	}

	public Stats getSatisfactionStats(){
		HashSet<Double> s = new HashSet<Double>();
		for(Agent agent : agents){
			if(agent.isDeleted()) continue;
			agent.computeSatisfaction();
			s.add(new Double(agent.getSatisfaction()));
		}
		double[] s_ = ArrayUtils.toPrimitive(s.toArray(new Double[s.size()]));
		Stats st = new Stats();
		st.max = StatUtils.max(s_);
		st.min = StatUtils.min(s_);
		st.mean = StatUtils.mean(s_);
		st.median = StatUtils.percentile(s_,50);
		st.q1 = StatUtils.percentile(s_,25);
		st.q2 = StatUtils.percentile(s_,75);
		st.std = Math.sqrt(StatUtils.variance(s_));
		st.rms = Math.sqrt(StatUtils.sumSq(s_)/s_.length);
		return st;
	}
	public class Stats{
		public double max,min,mean,median,q1,q2,std,rms;
		public void print(){
			System.out.println("Max = " + max);
			System.out.println("Min = " + min);
			System.out.println("Mean = " + mean);
			System.out.println("Median = " + median);
			System.out.println("Q1 = " + q1);
			System.out.println("Q2 = " + q2);
			System.out.println("Std = " + std);
			System.out.println("RMS = " + rms);
		}
	}




	//file logging capability
	private String logFilePath = null;
	private PrintWriter logWriter = null;
	private PrintWriter getLogWriter() {
		if(logWriter == null && logFilePath != null)
			try {
				File f = new File(logFilePath);
				if(f.exists()) f.delete();
				f.createNewFile();
				logWriter = new PrintWriter(logFilePath);
			} catch (Exception e) { e.printStackTrace(); }
		return logWriter;
	}

	private void closeLogger(){
		if(logWriter == null) return;
		logWriter.close();
		logWriter = null;
	}

}
