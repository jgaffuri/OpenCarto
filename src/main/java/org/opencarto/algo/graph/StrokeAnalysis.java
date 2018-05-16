/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;


/**
 * @author julien Gaffuri
 *
 */
public class StrokeAnalysis {

	private Graph g = null;

	private Collection<Stroke> strokes;
	public Collection<Stroke> getStrokes() { return strokes; }
	public class Stroke extends Feature {
		private List<Feature> sections;
		public List<Feature> getSections() { return sections; }
	}

	public StrokeAnalysis(Graph g) { this.g = g; }


	public StrokeAnalysis run(double maxDefletionAngleDeg) {

		//for each node, get list of candidate section pairs which are aligned
		HashMap<String,ArrayList<StrokeConnection>> nodeData = new HashMap<>();
		for(Node n : g.getNodes()) {
			//build all possible pairs and compute their defletion angle
			ArrayList<StrokeConnection> sps = new ArrayList<StrokeConnection>();
			List<Edge> es = new ArrayList<Edge>(); es.addAll(n.getEdges());
			for(int i=0; i<es.size(); i++)
				for(int j=i+1; j<es.size(); j++)
					sps.add( new StrokeConnection(n,es.get(i),es.get(j)) );

			//sort section pairs by defletion angle


			//get best section pairs

			//nodeData.put(n.getId(), sps);
		}

		//build stroke from section pairs
		strokes = new ArrayList<>();
		//initiate strokes with sections
		//while there are still pairs of sections
		//get pair with minimum defletion
		//merge strokes linking it

		return this;
	}


	public class StrokeConnection {
		Node n;
		Edge e1, e2;
		double defletionAngleDeg;
		StrokeConnection(Node n, Edge e1, Edge e2) {
			this.n=n; this.e1=e1; this.e2=e2;
			//TODO compute deflection angle in degree
		}

		//from 0 to 1 (perfectly salient)
		public double getSalience() {
			//TODO compute that depending on defletion angle and attributes
			return -1;
		}
	}

}
