package org.opencarto;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.processes.NoGeneralisation;
import org.opencarto.processes.SHPProcesses;
import org.opencarto.tiling.description.DefaultDescriptionBuilder;
import org.opencarto.tiling.description.DescriptionBuilder;


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
			@Override
			public String getDescription(Feature f) {
				StringBuffer sb = new StringBuffer();
				sb
				.append("<b>")
				.append(f.props.get("DENOM"))
				.append(" ")
				.append(f.props.get("NOM"))
				.append("</b>")
				.append("<br>")
				.append("Surface totale: ")
				.append((int)Double.parseDouble(f.props.get("S_CALCUL").toString()))
				.append("m2")
				;
				Integer an = (Integer) f.props.get("ANNEEC");
				if(an.intValue() != 0){
					sb
					.append("<br>")
					.append("Construit en ")
					.append(an);
				}
				return sb.toString();
			}
		};

		SHPProcesses.perform(inPath+"jardinWMClean_.shp", outPath+"jardin/", zs, new DefaultGeneralisation(), db, false );
	}

	public static void arbre(String inPath, String outPath, ZoomExtend zs) {
		SHPProcesses.perform(inPath+"arbresWMClean.shp", outPath+"arbre/", zs, new NoGeneralisation(), new DefaultDescriptionBuilder(), false);
	}

	public static void bati(String inPath, String outPath, ZoomExtend zs) {
		SHPProcesses.perform(inPath+"volumesbatisWMClean.shp", outPath+"bati/", zs, new DefaultGeneralisation(), new DefaultDescriptionBuilder(), false);
	}

}
