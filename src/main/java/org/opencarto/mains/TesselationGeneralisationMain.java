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

import com.vividsolutions.jts.geom.Point;

/**
 * @author julien Gaffuri
 *
 */
public class TesselationGeneralisationMain {

	public static void main(String[] args) {
		//https://stackoverflow.com/questions/15798936/creating-two-executable-jars-using-maven-assembly-plugin
		//https://stackoverflow.com/questions/8726884/create-multiple-runnable-jars-with-depencies-included-from-a-single-maven-proj

		//http://osgeo-org.1560.x6.nabble.com/java-lang-RuntimeException-Unable-to-find-function-Length-td4322100.html

		Options options = new Options();
		options.addOption(Option.builder("i").longOpt("inputFile").desc("Input file (SHP format).")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("o").longOpt("outputFile").desc("Output file (SHP format). Default: 'out.shp'.")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("ip").longOpt("inputPointFile").desc("Input file for points (SHP format).")
				.hasArg().argName("file").build());
		options.addOption(Option.builder("id").desc("Id property to link the units and the points.")
				.hasArg().argName("string").build());
		options.addOption(Option.builder("epsg").desc("EPSG code of the input data.")
				.hasArg().argName("string").build());
		options.addOption(Option.builder("s").longOpt("scaleDenominator").desc("The scale denominator for the target data. Default: 50000")
				.hasArg().argName("double").build());
		options.addOption(Option.builder("inb").longOpt("roundNb").desc("Number of iterations of the process. Default: 10.")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("mcn").longOpt("maxCoordinatesNumber").desc("Default: 1000000.")
				.hasArg().argName("int").build());
		options.addOption(Option.builder("omcn").longOpt("objMaxCoordinateNumber").desc("Default: 1000.")
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
		int epsg = cmd.getOptionValue("epsg") != null? Integer.parseInt(cmd.getOptionValue("epsg")) : -1;
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
			points = TesselationGeneralisation.loadPoints(inPtFile, idProp, 0);
		}

		System.out.println("Launch generalisation");
		units = TesselationGeneralisation.runGeneralisation(units, points, scaleDenominator, roundNb, maxCoordinatesNumber, objMaxCoordinateNumber);

		System.out.println("Save output to "+outFile);
		SHPUtil.saveSHP(units, outFile);

		System.out.println("End");
	}

}
