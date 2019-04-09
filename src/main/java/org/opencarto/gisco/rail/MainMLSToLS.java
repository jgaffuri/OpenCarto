package org.opencarto.gisco.rail;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.JTSGeomUtil;

public class MainMLSToLS {

	public static void main(String[] args) {
		System.out.println("Start");

		System.out.println("Load input");
		ArrayList<Feature> in = SHPUtil.loadSHP("/home/juju/Bureau/gisco_rail/in/RailwayLink.shp").fs;
		System.out.println(in.size());

		ArrayList<Feature> out = new ArrayList<Feature>();
		int nb = in.size();
		for(Feature f : in) {
			if(f.getGeom() == null || f.getGeom().isEmpty()) {
				nb--;
				continue;
			}

			if(f.getGeom().isSimple()) {
				out.add(f);
				continue;
			}

			nb--;
			for(Geometry g : JTSGeomUtil.getGeometries(f.getGeom())) {
				Feature f2 = new Feature();
				f2.getProperties().putAll(f.getProperties());
				f2.setGeom(g);
				out.add(f2);
				nb++;
			}
		}
		System.out.println(nb);

		System.out.println("Save output " + out.size());
		SHPUtil.saveSHP(out, "/home/juju/Bureau/gisco_rail/in/RailwayLinkClean.shp", SHPUtil.getCRS("/home/juju/Bureau/gisco_rail/in/RailwayLink.shp"));

		System.out.println("End");
	}

}
