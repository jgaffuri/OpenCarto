package org.opencarto.gisco.rail;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

public class MainMLSToLS {

	public static void main(String[] args) {
		System.out.println("Start");

		System.out.println("Load input");
		ArrayList<Feature> secs = SHPUtil.loadSHP("/home/juju/Bureau/gisco_rail/in/RailwayLink.shp").fs;
		System.out.println(secs.size());

		for(Feature f : secs) {
			System.out.println(f.getGeom().getClass().getSimpleName());
		}

		System.out.println("End");
	}

}
