/**
 * 
 */
package org.opencarto.transfoengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

/**
 * @author julien Gaffuri
 *
 */
public class Engine {

	private ArrayList<Agent> agents;

	public Engine(Collection<Agent> agents){
		this.agents = new ArrayList<Agent>(); this.agents.addAll(agents);
	}


	public void activateQueue(){
		for(Agent agent : agents) agent.activate();
	}

	//TODO implement/test other activation methods


	public Stats getSatisfactionStats(){
		HashSet<Double> s = new HashSet<Double>();
		for(Agent agent : agents){
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
		double max,min,mean,median,q1,q2,std,rms;
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

}
