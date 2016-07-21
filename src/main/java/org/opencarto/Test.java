package org.opencarto;

import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.processes.NoGeneralisation;
import org.opencarto.processes.SHPProcesses;

public class Test {

	public static void main(String[] args) throws Exception {
		System.out.println("Start");

		//String shpPath = "data/GEOFLA/COMMUNE.shp";
		//the_geom:MultiPolygon,INSEE_COM:INSEE_COM,NOM_COM:NOM_COM,STATUT:STATUT,X_CHF_LIEU:X_CHF_LIEU,Y_CHF_LIEU:Y_CHF_LIEU)
		String shpPath = "data/NUTS_2013_01M_SH/NUTS_BN_01M_2013.shp";
		//the_geom:MultiLineString,EU_FLAG:EU_FLAG,EFTA_FLAG:EFTA_FLAG,CC_FLAG:CC_FLAG,STAT_LEVL_:STAT_LEVL_,NUTS_BN_ID:NUTS_BN_ID,COAS_FLAG:COAS_FLAG,OTHR_CNTR_:OTHR_CNTR_,SHAPE_LEN:SHAPE_LEN)

		String outPath = "H:/desktop/tiles/";

		SHPProcesses.perform(shpPath, /*new String[]{"INSEE_COM","NOM_COM"}*/new String[]{"EU_FLAG","STAT_LEVL_"}, outPath, new ZoomExtend(2,5), new NoGeneralisation(), null/*new DefaultDescriptionBuilder()*/, true);

		System.out.println("Done.");
	}

}
