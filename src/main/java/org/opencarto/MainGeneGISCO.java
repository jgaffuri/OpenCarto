/**
 * 
 */
package org.opencarto;

import java.util.Collection;
import java.util.HashSet;

import org.geotools.feature.FeatureIterator;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphBuilder;
import org.opencarto.io.ShapeFile;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeneGISCO {

	public static void main(String[] args) {
		System.out.println("Start");

		//load statistical units
		Collection<MultiPolygon> units = new HashSet<MultiPolygon>();
		String nutsPath = "/home/juju/workspace/EuroGeoStat/resources/NUTS/2013/1M/LAEA/lvl3/RG.shp";
		ShapeFile shp = new ShapeFile(nutsPath);
		FeatureIterator<SimpleFeature> it = shp.getFeatures();
		while(it.hasNext())
			units.add((MultiPolygon) it.next().getDefaultGeometry());

		//structure dataset into topological map
		Graph graph = GraphBuilder.build(units );

		System.out.println(units.size());
		System.out.println(graph.getNodes().size());
		System.out.println(graph.getEdges().size());
		System.out.println(graph.getDomains().size());

		/*
		//simplify edges one by one checking the units are ok
		for(Edge e : topoMap.getEdges()) {
			//if units are not ok, try another edge OR reduce simplification OR collapse edge if too small
		}*/

		//save edges as shp file
		ShapeFile shpEdges = new ShapeFile("LineString", 3035, "", "/home/juju/Bureau/out/", "edges.shp", true,true,true);
		for(Edge e : graph.getEdges())
			shpEdges.add(e.getGeometry());

		System.out.println("End");
	}



	//*** constraints/measure/algo
	//A. border granularity / minimum segment size / simplification: DP, wis, cusmoo, etc.
	//B. border topology: no self overlap / topological query / none
	//C. unit topology: no self overlap / topological query / none
	//D. border minimum size / length / segment enlargement (GAEL) - segment colapse (integrate, GAEL)
	//E. unit area (or part) / area / scaling (GAEL), area colapse (integrate, GAEL), skeletisation
	//F. unit shape / convexity, elongation, etc. / none
	//G. border shape & position / Hausdorf distance / none

	//*** generalisation engine
	//agents: borders and units
	//evaluation: based on constraints measures and severity functions
	//activation strategies:
	// 1. meso-border: one border + two units
	// 2. meso-unit: one unit + neighbor units

	//evaluate all constraints - evaluate all agents
	//select (randomly) an unsatisfied agent (unit or border)
	//evaluate meso satisfaction (simply average of components' satisfaction)
	//get best algo to apply, apply it
	//if result is improved, keep it or go back to previous step


	//can be adapted for cartogram generation?

}
