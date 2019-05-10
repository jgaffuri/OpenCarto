/**
 * 
 */
package org.opencarto.algo.graph;

import org.opencarto.algo.distances.HausdorffDistance;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;

/**
 * 
 * Various algorithms on graphs.
 * 
 * @author julien Gaffuri
 *
 */
public class GraphUtils {


	//remove edges with similar geometries (based on haussdorff distance)
	//the edges are supposed not to be linked to any face.
	public static void removeSimilarDuplicateEdges(Graph g, double haussdorffDistance) {
		Edge e = findSimilarDuplicateEdgeToRemove(g, haussdorffDistance);
		while(e != null) {
			g.remove(e);
			e = findSimilarDuplicateEdgeToRemove(g, haussdorffDistance);
		}
	}

	public static Edge findSimilarDuplicateEdgeToRemove(Graph g, double haussdorffDistance) {
		for(Edge e : g.getEdges()) {
			for(Edge e_ : e.getN1().getOutEdges())
				if(e!=e_ && e_.getN2() == e.getN2() && new HausdorffDistance(e.getGeometry(),e_.getGeometry()).getDistance()<haussdorffDistance)
					return getLongest(e,e_);
			for(Edge e_ : e.getN2().getOutEdges())
				if(e!=e_ && e_.getN2() == e.getN1() && new HausdorffDistance(e.getGeometry(),e_.getGeometry()).getDistance()<haussdorffDistance)
					return getLongest(e,e_);
		}
		return null;
	}

	public static Edge getLongest(Edge e1, Edge e2) {
		double d1 = e1.getGeometry().getLength();
		double d2 = e2.getGeometry().getLength();
		if(d1<d2) return e2; else return e1;
	}


}
