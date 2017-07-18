/**
 * 
 */
package org.opencarto.io;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Domain;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class GraphSHPUtil {

	public static void exportNodesAsSHP(Graph g, String outPath, String outFile, int epsg){
		ArrayList<Feature> fs = new ArrayList<Feature>();
		for(Node n:g.getNodes()) {
			Feature f = n.toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

	public static void exportEdgesAsSHP(Graph g, String outPath, String outFile, int epsg){
		ArrayList<Feature> fs = new ArrayList<Feature>();
		for(Edge e:g.getEdges()){
			Feature f = e.toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}

	public static void exportDomainsAsSHP(Graph g, String outPath, String outFile, int epsg){
		ArrayList<Feature> fs = new ArrayList<Feature>();
		for(Domain d:g.getDomains()) {
			Feature f = d.toFeature();
			f.setProjCode(epsg);
			fs.add(f);
		}
		SHPUtil.saveSHP(fs, outPath, outFile);
	}


	/*
	DefaultFeatureCollection fs;
	ShapeFile shp;

	//save nodes as shp file
	shp = new ShapeFile("Point", 3035, "", outPath , "nodes.shp", true,true,true);
	fs = new DefaultFeatureCollection(null, shp.getSchema());
	for(Node n : graph.getNodes())
		fs.add(shp.buildFeature(n.getGeometry()));
	shp.add(fs);


	shp = new ShapeFile("LineString", 3035, "", outPath, "edges.shp", true,true,true);
	fs = new DefaultFeatureCollection(null, shp.getSchema());
	for(Edge e : graph.getEdges())
		fs.add(shp.buildFeature(e.getGeometry()));
	shp.add(fs);

	//save domains as shp file
	shp = new ShapeFile("Polygon", 3035, "", outPath, "domains.shp", true,true,true);
	fs = new DefaultFeatureCollection(null, shp.getSchema());
	for(Domain d : graph.getDomains())
		fs.add(shp.buildFeature(d.getGeometry()));
	shp.add(fs);*/

}
