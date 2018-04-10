package org.opencarto.z;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TestParams {
	//https://stackoverflow.com/questions/367706/how-do-i-parse-command-line-arguments-in-java
	//http://commons.apache.org/proper/commons-cli/
	//http://commons.apache.org/proper/commons-cli/usage.html

	public static void main(String[] args) {
		System.out.println("Start");

		Options options = new Options();
		options.addOption(Option.builder("h").longOpt("help")
				.desc(  "print this help message" ).build());
		options.addOption("t", false, "time");
		options.addOption("c", true, "country code");
		options.addOption(Option.builder("logfile").argName("file")
				.hasArg()
				.desc(  "use given file for log" ).build()
				);


		CommandLine cmd = null;
		try { cmd = new DefaultParser().parse( options, args); } catch (ParseException e) {
			System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
			return;
		}

		//help statement
		if(cmd.hasOption("help")) {
			new HelpFormatter().printHelp("ant", options );
			return;
		}

		//get t option value
		boolean tOpt = cmd.hasOption("t");
		//get c option value
		String cOpt = cmd.getOptionValue("c");
		if(cOpt == null) { cOpt = "default"; }

		System.out.println(tOpt);
		System.out.println(cOpt);

		System.out.println("End");
	}

}
