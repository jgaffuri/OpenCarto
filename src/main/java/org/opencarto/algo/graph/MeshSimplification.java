/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.HashSet;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.opencarto.algo.base.Union;
import org.opencarto.algo.resolutionise.Resolutionise;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.util.FeatureUtil;
import org.opencarto.util.JTSGeomUtil;

/**
 * Some functions to simplify linear meshes.
 * The input is a collection of lines, possibly intersecting, which form a mesh to be simplified.
 * 
 * @author julien Gaffuri
 *
 */
public class MeshSimplification {

	//TODO better document


	/**
	 * Run JTS line merger (see JTS doc)
	 * 
	 * @param lines
	 * @return
	 */
	public static Collection lineMerge(Collection lines) {
		LineMerger lm = new LineMerger();
		lm.add(lines);
		return lm.getMergedLineStrings();
	}

	/**
	 * @param lines
	 * @return
	 */
	public static Collection planifyLines(Collection lines) {
		Geometry u = Union.getLineUnion(lines);
		return JTSGeomUtil.getLineStringGeometries(u);
	}

	/**
	 * Apply Ramer-Douglas-Peucker filter.
	 * 
	 * @param lines
	 * @param d
	 * @return
	 */
	public static Collection DPsimplify(Collection lines, double d) {
		Collection out = new HashSet<Geometry>();
		for(Object line : lines) out.add( DouglasPeuckerSimplifier.simplify((Geometry)line, d) );
		return out;
	}





	/*
	public static Collection deleteFlatTriangles(Collection lines, double d) {
		Graph g = GraphBuilder.buildFromLinearFeaturesPlanar( linesToFeatures(lines), true );
		deleteFlatTriangles(g, d);
		return getEdgeGeometries(g.getEdges());
	}

	public static void deleteFlatTriangles(Graph g, double d) {
		Edge e = findEdgeToDeleteForFlatTriangle(g, d);
		while(e != null) {
			if(e.f1!=null) g.remove(e.f1);
			if(e.f2!=null) g.remove(e.f2);
			g.remove(e);
			e = findEdgeToDeleteForFlatTriangle(g, d);
		}
	}

	public static Edge findEdgeToDeleteForFlatTriangle(Graph g, double d) {

		//TODO
		/*for(Face f : g.getFaces()) {
			if(f.getNodes().size() > 3) continue;
			Edge e = f.getLongestEdge();
			//TODO measure minimum heigth and compare to d
			double h = Math.abs(()*()-()*()) / e.getGeometry().getLength();
			if(h>d) continue;
			return e;
		}*/
	//return null;
	//}


	public static Collection removeSimilarDuplicateEdges(Collection lines, double haussdorffDistance) {
		Graph g = GraphBuilder.buildFromLinearFeaturesNonPlanar( FeatureUtil.geometriesToFeatures(lines) );
		GraphUtils.removeSimilarDuplicateEdges(g, haussdorffDistance);
		return getEdgeGeometries(g.getEdges());
	}




	public static Collection dtsePlanifyLines(Collection lines, double res) {
		lines = deleteTooShortEdges(lines, res);
		lines = planifyLines(lines);
		int sI=1,sF=0;
		while(sF<sI) {
			System.out.println(" dtsePlanifyLines loop " + lines.size());
			sI=lines.size();
			lines = deleteTooShortEdges(lines, res);
			lines = planifyLines(lines);
			sF=lines.size();
		}
		return lines;
	}

	public static Collection deleteTooShortEdges(Collection lines, double d) {
		//create graph
		Graph g = GraphBuilder.buildFromLinearFeaturesNonPlanar( FeatureUtil.geometriesToFeatures(lines) );
		EdgeCollapse.collapseTooShortEdges(g, d);
		return getEdgeGeometries(g.getEdges());
	}

	public static Collection<Geometry> resPlanifyLines(Collection<Geometry> lines, double res) {
		lines = Resolutionise.applyLinear(lines, res);
		lines = planifyLines(lines);
		int sI=1,sF=0;
		while(sF<sI) {
			System.out.println(" resPlanifyLines loop " + lines.size());
			sI=lines.size();
			lines = Resolutionise.applyLinear(lines, res);
			lines = planifyLines(lines);
			sF=lines.size();
		}
		return lines;
	}




	public static Collection<LineString> getEdgeGeometries(Collection<Edge> es) {
		Collection<LineString> out = new HashSet<>();
		for(Edge e : es) out.add(e.getGeometry());
		return out;
	}

}
