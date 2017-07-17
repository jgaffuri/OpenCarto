/**
 * 
 */
package org.opencarto;

import org.opencarto.datamodel.graph.Graph;

/**
 * @author julien Gaffuri
 *
 */
public class MainGeneGISCO {

	public static void main(String[] args) {

		//load statistical units

		//structure dataset into topological map
		Graph topoMap;

		//simplify edges one by one checking the units are ok
		//if units are not ok, try another edge OR reduce simplification OR collapse edge if too small


		//save domains as shp file



	
	
	
	
	
	
	
	
	}

	
	
	//*** data model
	//topological map - graph with point/line/area. Link to features borders/units.
	//gene algorithms should be applicable on that

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
