package org.opencarto;

import java.util.ArrayList;

import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;

public class MainNUTSExtraction {

	public static void main(String[] args) {
		String outPath = "~/Bureau/drafts/cnts/";

		//load nuts regions
		ArrayList<Feature> fs = SHPUtil.loadSHP("~/Bureau/drafts/NUTS_RG_2016_RG_01M_DRAFT.shp", 4258).fs;

		for(String cnt : new String[] {"FR","BE","DE"}) {
			//make country folder
			//filter - nuts 3 regions for cnt
			//save as SHP
			//make and save all other levels? make boundaries?
			//make overview image
			//zip everything
		}

	}

}
