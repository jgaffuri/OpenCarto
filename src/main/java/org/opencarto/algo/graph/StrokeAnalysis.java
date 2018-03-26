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
			//get pairs of edges
			//evaluate pair of edge
			Edge e1,e2;
			SectionPair sp = evaluate(n,e1,e2);
			if(sp==null) continue;
			ArrayList<SectionPair> sps = new ArrayList<SectionPair>();
			sps .add(sp);
		}

		//go through list of pairs and build strokes as list of sections
		strokes = new ArrayList<>();
		return this;
	}

	private SectionPair evaluate(Node n, Edge e1, Edge e2) {
		//if ok, return section pair else return null
		return null;
	}

	public class SectionPair {
		Node n;
		Edge e1, e2;
		double deflectionAngle;
	}


	private Collection<Stroke> strokes;
	public Collection<Stroke> getStrokes() { return strokes; }


	public class Stroke extends Feature {
		private List<Feature> sections;
		public List<Feature> getSections() { return sections; }
	}

}
