/**
 * 
 */
package org.opencarto;

import org.opencarto.io.ShapeFile;

/**
 * 
 * Test of NUTS generalisation based on resolutionise operation
 * 
 * @author julien Gaffuri
 *
 */
public class MainNUTSResolutionise {

	public static void main(String[] args) {
		//load nuts regions and boundaries from shapefile
		ShapeFile rg = new ShapeFile("data/NUTS_2013_01M_SH/NUTS_RG_01M_2013.shp", true);
		System.out.println(rg.count());
		System.out.println(rg.getSchema());

		//compute generalisation

		//save
	}

}
