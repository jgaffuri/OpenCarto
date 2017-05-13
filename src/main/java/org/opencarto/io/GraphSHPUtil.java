/**
 * 
 */
package org.opencarto.io;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class GraphSHPUtil {

	public static void exportEdgesAsSHP(Graph g, String outPath, String outFile, int epsg){
		ArrayList<Feature> fs = new ArrayList<Feature>();
		for(Edge e:g.getEdges()){
			Feature f = e.toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

	public static void exportNodesAsSHP(Graph g, String outPath, String outFile, int epsg){
		ArrayList<Feature> fs = new ArrayList<Feature>();
		for(Node n:g.getNodes()) {
			Feature f = n.toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

}
