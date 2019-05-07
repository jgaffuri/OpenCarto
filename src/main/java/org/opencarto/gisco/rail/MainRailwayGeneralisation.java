/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayGeneralisation {

	public static void main(String[] args) throws Exception {
		System.out.println("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		ArrayList<Feature> secs = SHPUtil.loadSHP(basePath+"out/EM/RailwayLinkEM.shp").fs;
		System.out.println(secs.size());

		
		
		
		


		System.out.println("End");
	}

}
