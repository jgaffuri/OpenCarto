/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;

/**
 * @author julien Gaffuri
 *
 */
public class StrokeAnalysis {

	private Graph g = null;

	public StrokeAnalysis(Graph g) {
		this.g = g;
	}

	public StrokeAnalysis run() {
		//for each node, attach list of section pairs, which are "aligned" (angle of deflection)
		//go through list of pairs and build strokes as list of sections
		strokes = new ArrayList<>();
		return this;
	}

	private Collection<Stroke> strokes;
	public Collection<Stroke> getStrokes() { return strokes; }


	public class Stroke extends Feature {
		private List<Feature> sections;
		public List<Feature> getSections() { return sections; }
	}

}
