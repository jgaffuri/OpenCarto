package org.opencarto.gisco.rail;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.FeatureUtil;

public class MainMLSToLS {

	public static void main(String[] args) {
		System.out.println("Start");

		System.out.println("Load input");
		ArrayList<Feature> in = SHPUtil.loadSHP("/home/juju/Bureau/gisco_rail/in/RailwayLink.shp").fs;
		System.out.println(in.size());

		ArrayList<Feature> out = FeatureUtil.getFeaturesWithSimpleGeometrie(in);
		System.out.println("Save output " + out.size());
		SHPUtil.saveSHP(out, "/home/juju/Bureau/gisco_rail/in/RailwayLinkClean.shp", SHPUtil.getCRS("/home/juju/Bureau/gisco_rail/in/RailwayLink.shp"));

		System.out.println("End");
	}

}
