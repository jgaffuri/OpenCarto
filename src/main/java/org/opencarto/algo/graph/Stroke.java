/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.List;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Graph;

/**
 * @author julien Gaffuri
 *
 */
public class Stroke extends Feature {

	private List<Feature> sections;
	public List<Feature> getSections() { return sections; }

	public static Collection<Stroke> get(Graph g) {
		//for each node, attach list of section pairs, which are "aligned" (angle of deflection)
		//go through list of pairs and build strokes as list of sections
		return null;
	}

}
