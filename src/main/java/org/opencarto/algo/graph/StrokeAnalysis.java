/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.ArrayList;
import java.util.Collection;
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
		public Stroke(Stroke_ s) {
			//TODO set sections, geometry and salience (as a property)
			this.set("s", s.getSalience());
		}
		private List<Feature> sections = new ArrayList<>();
		public List<Feature> getSections() { return sections; }
	}

	public StrokeAnalysis(Graph g) { this.g = g; }



	//for the computation only
	private class Stroke_ {
		Stroke_(Edge e) { sections.add(e); }
		List<Edge> sections = new ArrayList<>();
		double getSalience() {
			//TODO depends on length ?
			return -1;
		}
	}

	public class StrokeConnection {
		Node n;
		Stroke s1, s2;
		double defletionAngleDeg;
		StrokeConnection(Node n, Stroke s1, Stroke s2) {
			this.n=n; this.s1=s1; this.s2=s2;
			//TODO compute deflection angle in degree + salience
		}
		double sal;
	}




	public StrokeAnalysis run(double maxDefletionAngleDeg) {

		//build initial list of strokes with single edges
		Collection<Stroke_> sts = new ArrayList<>();
		for(Edge e: g.getEdges()) sts.add(new Stroke_(e));

		//get possible connections
		Collection<StrokeConnection> cs = getPossibleConnections(sts, maxDefletionAngleDeg);

		//build strokes iterativelly starting with the best connection
		StrokeConnection c = getBestConnection(cs);
		while(c != null) {
			Stroke_ sNew = merge(c);
			sts.remove(c.s1); sts.remove(c.s2);
			sts.add(sNew);
			//TODO remove all connections around c node, which involve s1 or s2
			c = getBestConnection(cs);
		}

		//build this.strokes
		strokes = new ArrayList<>();
		for(Stroke_ s_ : sts) strokes.add(new Stroke(s_));

		return this;
	}

}
