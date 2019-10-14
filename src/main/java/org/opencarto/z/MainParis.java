package org.opencarto.z;

import org.opencarto.ZoomExtend;
import org.opencarto.tiling.description.DescriptionBuilder;

import eu.europa.ec.eurostat.eurogeostat.datamodel.Feature;


public class MainParis {

	public static void main(String[] args) {
		System.out.println("Start");

		ZoomExtend zs = new ZoomExtend(0,19);
		String inPath = "/home/juju/Bureau/docs/donnees/paris/";
		String outPath = "/home/juju/workspace/opencarto-code/client/war/data/paris/";

		//cleaning
		//SHPUtil.cleanGeometries(inPath+"jardinWM.shp", "the_geom", inPath, "jardinWMClean.shp");
		//SHPUtil.cleanGeometries(inPath+"volumesbatisWM.shp", "the_geom", inPath, "volumesbatisWMClean.shp");
		//SHPUtil.cleanGeometries(inPath+"arbresWM.shp", "the_geom", inPath, "arbresWMClean.shp");

		jardin(inPath, outPath, zs);
		//bati(inPath, outPath, zs);
		//arbre(inPath, outPath, zs);

		System.out.println("Done.");
	}

	public static void jardin(String inPath, String outPath, ZoomExtend zs) {
		DescriptionBuilder db = new DescriptionBuilder() {
			public String getDescription(Feature f) {
				StringBuffer sb = new StringBuffer();
				sb
				.append("<b>")
				.append(f.getAttribute("DENOM"))
				.append(" ")
				.append(f.getAttribute("NOM"))
				.append("</b>")
				.append("<br>")
				.append("Surface totale: ")
				.append((int)Double.parseDouble(f.getAttribute("S_CALCUL").toString()))
				.append("m2")
				;
				Integer an = (Integer) f.getAttribute("ANNEEC");
				if(an.intValue() != 0){
					sb
					.append("<br>")
					.append("Construit en ")
					.append(an);
				}
				return sb.toString();
			}
		};

		//SHPProcesses.perform(inPath+"jardinWMClean_.shp", outPath+"jardin/", zs, new DefaultGeneralisation(), db, false, null );
	}

	public static void arbre(String inPath, String outPath, ZoomExtend zs) {
		//SHPProcesses.perform(inPath+"arbresWMClean.shp", outPath+"arbre/", zs, new NoGeneralisation(), new DefaultDescriptionBuilder(), false, null);
	}

	public static void bati(String inPath, String outPath, ZoomExtend zs) {
		//SHPProcesses.perform(inPath+"volumesbatisWMClean.shp", outPath+"bati/", zs, new DefaultGeneralisation(), new DefaultDescriptionBuilder(), false, null);
	}

}
