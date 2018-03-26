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

	public StrokeAnalysis(Graph g) { this.g = g; }

	public StrokeAnalysis run() {

		//for each node, get list of section pairs, which are "aligned" (small angle of deflection)
		HashMap<String,ArrayList<SectionPair>> nodeData = new HashMap<>();
		for(Node n : g.getNodes()) {
			ArrayList<SectionPair> sps = new ArrayList<SectionPair>();
			//evaluate each pair of edge
			Edge e1=null,e2=null;
			{
				SectionPair sp = evaluate(n,e1,e2);
				if(sp==null) continue;
				sps.add(sp);
			}
			nodeData.put(n.getId(), sps);
		}

		//build stroke from section pairs
		strokes = new ArrayList<>();
		//initiate strokes with sections
		//while there are still pairs of sections
		//get pair with minimum defletion
		//merge strokes linking it

		return this;
	}

	public class SectionPair {
		Node n;
		Edge e1, e2;
		double deflectionAngle;
		SectionPair(Node n, Edge e1, Edge e2) {
			this.n=n; this.e1=e1; this.e2=e2;
			//TODO compute deflection angle in radian
		}
	}


	private Collection<Stroke> strokes;
	public Collection<Stroke> getStrokes() { return strokes; }


	public class Stroke extends Feature {
		private List<Feature> sections;
		public List<Feature> getSections() { return sections; }
	}

}
