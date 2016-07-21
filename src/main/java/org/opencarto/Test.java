package org.opencarto;

import org.opencarto.io.GeoJSONUtil;

public class Test {

	public static void main(String[] args) throws Exception {
		String path = "data/GEOFLA/COMMUNE.shp";
		//the_geom:MultiPolygon   INSEE_COM:INSEE_COM   NOM_COM:NOM_COM   STATUT:STATUT   X_CHF_LIEU:X_CHF_LIEU   Y_CHF_LIEU:Y_CHF_LIEU

		//TODO test/complete vector tiling
		//TODO remove unnecessary attributes
		GeoJSONUtil.toGeoJSON(path,"communes.json");

		System.out.println("Done.");
	}

}
