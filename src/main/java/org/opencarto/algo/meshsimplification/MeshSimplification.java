/**
 * 
 */
package org.opencarto.algo.meshsimplification;

import java.util.Collection;
import java.util.HashSet;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.opencarto.algo.base.Union;
import org.opencarto.algo.graph.GraphConnexComponents;
import org.opencarto.algo.resolutionise.Resolutionise;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.util.JTSGeomUtil;

/**
 * Some functions to simplify linear meshes
 * 
 * @author julien Gaffuri
 *
 */
public class MeshSimplification {

	//TODO better document


	public static Collection lineMerge(Collection lines) {
		LineMerger lm = new LineMerger();
		lm.add(lines);
		return lm.getMergedLineStrings();
	}

	public static Collection planifyLines(Collection lines) {
		Geometry u = Union.getLineUnion(lines);
		return JTSGeomUtil.getLineStringGeometries(u);
	}

	public static Collection filterGeom(Collection lines, double d) {
		Collection out = new HashSet<Geometry>();
		for(Object line : lines) out.add( DouglasPeuckerSimplifier.simplify((Geometry)line, d) );
		return out;
	}






	public static Collection deleteFlatTriangles(Collection lines, double d) {
		//create graph
		Graph g = GraphBuilder.buildForNetworkFromLinearFeatures( linesToFeatures(lines) );
		deleteFlatTriangles(g, d);
		return g.getEdgeGeometries();
	}

	//TODO move to graph
	public static void deleteFlatTriangles(Graph g, double d) {
		Edge e = findEdgeToDeleteForFlatTriangle(g, d);
		while(e != null) {
			if(e.f1!=null) g.remove(e.f1);
			if(e.f2!=null) g.remove(e.f2);
			g.remove(e);
			e = findEdgeToDeleteForFlatTriangle(g, d);
		}
	}

	//TODO move to graph
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
		return null;
	}



	public static Collection keepOnlyLargestGraphConnexComponents(Collection lines, int minEdgeNumber) {
		Graph g = GraphBuilder.buildForNetworkFromLinearFeaturesNonPlanar( linesToFeatures(lines) );
		Collection<Graph> ccs = GraphConnexComponents.get(g);
		Collection out = new HashSet();
		for(Graph cc : ccs) {
			if( cc.getEdges().size() < minEdgeNumber ) continue;
			for(Edge e : cc.getEdges())
				out.add(e.getGeometry());
		}
		return out;
	}

	public static Collection removeSimilarDuplicateEdges(Collection lines, double haussdorffDistance) {
		Graph g = GraphBuilder.buildForNetworkFromLinearFeaturesNonPlanar( linesToFeatures(lines) );
		g.removeSimilarDuplicateEdges(haussdorffDistance);
		return g.getEdgeGeometries();
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
		Graph g = GraphBuilder.buildForNetworkFromLinearFeaturesNonPlanar( linesToFeatures(lines) );
		g.collapseTooShortEdges(d);
		return g.getEdgeGeometries();
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


	public static Collection featuresToLines(Collection fs) {
		Collection lines = new HashSet<Geometry>();
		for(Object f : fs) lines.add(((Feature)f).getGeom());
		return lines;
	}

	public static HashSet<Feature> linesToFeatures(Collection lines) {
		HashSet<Feature> fs = new HashSet<Feature>();
		int i=0;
		for(Object ls : lines) {
			Feature f = new Feature();
			f.id = ""+(i++);
			f.setGeom((Geometry)ls);
			fs.add(f);
		}
		return fs;
	}

}
