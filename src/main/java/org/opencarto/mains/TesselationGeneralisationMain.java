/**
 * 
 */
package org.opencarto.mains;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opencarto.transfoengine.tesselationGeneralisation.TesselationGeneralisation;
import org.opencarto.util.ProjectionUtil.CRSType;

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class TesselationGeneralisationMain {

	public static void main(String[] args) {
		//https://sourceforge.net/projects/geotools/files/

		//https://stackoverflow.com/questions/15798936/creating-two-executable-jars-using-maven-assembly-plugin
		//https://stackoverflow.com/questions/8726884/create-multiple-runnable-jars-with-depencies-included-from-a-single-maven-proj?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
		//https://stackoverflow.com/questions/8726884/create-multiple-runnable-jars-with-depencies-included-from-a-single-maven-proj?rq=1
		//https://stackoverflow.com/questions/8726884/create-multiple-runnable-jars-with-depencies-included-from-a-single-maven-proj/8726969#8726969

		//http://osgeo-org.1560.x6.nabble.com/java-lang-RuntimeException-Unable-to-find-function-Length-td4322100.html

		Options options = new Options();
		options.addOption(Option.builder("i").longOpt("inputFile").desc("Input file (SHP format)")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("o").longOpt("outputFile").desc("Output file (SHP format)")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("ip").longOpt("inputPointFile").desc("Input file for points (SHP format)")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("id").desc("Id property to link the units and the points")
				.hasArg().argName("string").build());
		options.addOption(Option.builder("crs").desc("The EPSG code of the CRS")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("crst")
				.hasArg().argName("string").build());
		options.addOption(Option.builder("s").longOpt("scaleDenominator").desc("The scale denominator for the target data")
				.hasArg().argName("double").build());
		options.addOption(Option.builder("inb").longOpt("roundNb").desc(  "Number of iterations of the process" )
				.hasArg().argName("int").build());
		options.addOption(Option.builder("mcn").longOpt("maxCoordinatesNumber")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("omcn").longOpt("objMaxCoordinateNumber")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("h").desc("Show this help message").build());

		CommandLine cmd = null;
		try { cmd = new DefaultParser().parse( options, args); } catch (ParseException e) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			return;
		}

		//help statement
		if(cmd.hasOption("h")) {
			new HelpFormatter().printHelp("ant", options);
			return;
		}

		//String inFile = "src/test/resources/testTesselationGeneralisation.shp";
		String inFile = cmd.getOptionValue("i");
		if(inFile==null) {
			System.err.println("An input file should be specified with -i option. See -h for more detail on the options.");
			return;
		} else if(!new File(inFile).exists()) {
			System.err.println("Input file does not exist: "+inFile);
			return;
		}
		String outFile = cmd.getOptionValue("o");
		if(outFile == null) outFile = new File(inFile).getParent() + "/out.shp";
		String inPtFile = cmd.getOptionValue("ip");
		String idProp = cmd.getOptionValue("id");
		int epsg = cmd.getOptionValue("crs") != null? Integer.parseInt(cmd.getOptionValue("crs")) : -1;
		//CRSType.CARTO
		CRSType crsType = CRSType.CARTO; //TODO
		double scaleDenominator = cmd.getOptionValue("s") != null? Integer.parseInt(cmd.getOptionValue("s")) : 50000;
		int roundNb = cmd.getOptionValue("inb") != null? Integer.parseInt(cmd.getOptionValue("inb")) : 10;
		int maxCoordinatesNumber = cmd.getOptionValue("mcn") != null? Integer.parseInt(cmd.getOptionValue("mcn")) : 1000000;
		int objMaxCoordinateNumber = cmd.getOptionValue("omcn") != null? Integer.parseInt(cmd.getOptionValue("omcn")) : 1000;


		System.out.println("Load data from "+inFile);
		Collection<Feature> units = SHPUtil.loadSHP(inFile, epsg).fs;
		if(idProp != null && !"".equals(idProp)) for(Feature unit : units) unit.id = unit.getProperties().get(idProp).toString();

		HashMap<String, Collection<Point>> points = null;
		if(inPtFile != null && !"".equals(inPtFile)) {
			System.out.println("Load point data from "+inPtFile);
			points = TesselationGeneralisation.loadPoints(inPtFile, idProp);
		}

		System.out.println("Launch generalisation");
		units = TesselationGeneralisation.runGeneralisation(units, points, crsType, scaleDenominator, roundNb, maxCoordinatesNumber, objMaxCoordinateNumber);

		System.out.println("Save output to "+outFile);
		SHPUtil.saveSHP(units, outFile);

		System.out.println("End");
	}

}
