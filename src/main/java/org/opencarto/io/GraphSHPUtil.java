/**
 * 
 */
package org.opencarto.io;

import org.opencarto.datamodel.graph.Graph;

/**
 * @author julien Gaffuri
 *
 */
public class GraphSHPUtil {

	public static void exportAsSHP(Graph g, String outPath, int epsg){
		GraphSHPUtil.exportDomainsAsSHP(g, outPath, "domains.shp", epsg);
		GraphSHPUtil.exportEdgesAsSHP(g, outPath, "edges.shp", epsg);
		GraphSHPUtil.exportNodesAsSHP(g, outPath, "nodes.shp", epsg);
	}

	public static void exportDomainsAsSHP(Graph g, String outPath, String outFile, int epsg){
		SHPUtil.saveSHP(g.getDomainFeatures(epsg), outPath, outFile);
	}

	public static void exportEdgesAsSHP(Graph g, String outPath, String outFile, int epsg){
		SHPUtil.saveSHP(g.getEdgeFeatures(epsg), outPath, outFile);
	}

	public static void exportNodesAsSHP(Graph g, String outPath, String outFile, int epsg){
		SHPUtil.saveSHP(g.getNodeFeatures(epsg), outPath, outFile);
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
