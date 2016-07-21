package org.opencarto;

import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.processes.SHPProcesses;

public class Test {

	public static void main(String[] args) throws Exception {
		System.out.println("Start");
		String outPath = "H:/desktop/tiles/";
		//System.out.println(SHPUtil.getSchema(shpPath));

		//String shpPath = "data/GEOFLA/COMMUNE.shp";
		//the_geom:MultiPolygon,INSEE_COM:INSEE_COM,NOM_COM:NOM_COM,STATUT:STATUT,X_CHF_LIEU:X_CHF_LIEU,Y_CHF_LIEU:Y_CHF_LIEU)
		//SHPProcesses.perform(shpPath, new String[]{"INSEE_COM","NOM_COM"}, outPath, new ZoomExtend(2,5), new NoGeneralisation(), null, true);

		//String shpPath = "data/NUTS_2013_01M_SH/NUTS_BN_01M_2013.shp";
		//the_geom:MultiLineString,EU_FLAG:EU_FLAG,EFTA_FLAG:EFTA_FLAG,CC_FLAG:CC_FLAG,STAT_LEVL_:STAT_LEVL_,NUTS_BN_ID:NUTS_BN_ID,COAS_FLAG:COAS_FLAG,OTHR_CNTR_:OTHR_CNTR_,SHAPE_LEN:SHAPE_LEN)
		//SHPProcesses.perform(shpPath, new String[]{"EU_FLAG","STAT_LEVL_"}, outPath, new ZoomExtend(2,5), new NoGeneralisation(), null, true);

		String shpPath = "data/CNTR_2014_03M_SH/CNTR_RG_03M_2014.shp";
		//SimpleFeatureTypeImpl CNTR_RG_03M_2014 identified extends polygonFeature(the_geom:MultiPolygon,CNTR_ID:CNTR_ID,SHAPE_AREA:SHAPE_AREA,SHAPE_LEN:SHAPE_LEN)
		SHPProcesses.perform(shpPath, new String[]{"CNTR_ID","SHAPE_AREA","SHAPE_LEN"}, outPath, new ZoomExtend(2,4), new DefaultGeneralisation(), null/*new DefaultDescriptionBuilder()*/, true);

		System.out.println("Done.");
	}

}
