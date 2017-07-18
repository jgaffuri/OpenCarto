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
import org.opencarto.io.GraphSHPUtil;
import org.opencarto.io.ShapeFile;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeneGISCO {

	public static void main(String[] args) {
		System.out.println("Start");

		String nutsPath = "/home/juju/workspace/EuroGeoStat/resources/NUTS/2013/1M/LAEA/lvl3/RG.shp";
		String outPath = "/home/juju/Bureau/out/";

		//load statistical units
		Collection<MultiPolygon> units = new HashSet<MultiPolygon>();
		ShapeFile nutsSHP = new ShapeFile(nutsPath);
		FeatureIterator<SimpleFeature> it = nutsSHP.getFeatures();
		while(it.hasNext())
			units.add((MultiPolygon) it.next().getDefaultGeometry());

		//structure dataset into topological map
		Graph graph = GraphBuilder.build(units);

		System.out.println(units.size());
		System.out.println("nodes: "+graph.getNodes().size());
		System.out.println("edges: "+graph.getEdges().size());
		System.out.println("domains: "+graph.getDomains().size());


		//simplify edges
		double resolution = 2000;
		for(Edge e : graph.getEdges()) {
			try {
				//apply douglass peucker algorithm
				LineString ls = e.getGeometry();
				//ls = (LineString) DouglasPeuckerSimplifier.simplify(ls, resolution);
				//ls = (LineString) GaussianSmoothing.get(ls, resolution, 200);
				e.coords = ls.getCoordinates();
				e.coords[0]=e.getN1().c;
				e.coords[e.coords.length-1]=e.getN2().c;
			} catch (Exception e1) {}
		}


		//save output as shp files
		GraphSHPUtil.exportAsSHP(graph, outPath, 3035);

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
