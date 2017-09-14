package org.opencarto.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;


/**
 * @author julien Gaffuri
 *
 */
public class Util {

	//print stack (for debugging)
	public static void printStackOut(){ printStack(System.out);}
	public static void printStackErr(){ printStack(System.err);}
	public static void printStack(PrintStream ps){
		boolean first=true;
		for(StackTraceElement se : Thread.currentThread().getStackTrace()){
			ps.println((first?"":"--- ")+se.toString()); first=false;
		}
	}

	//print progress in %
	public static void printProgress(int nbDone, int nbTot) {
		int ratio = 100*nbDone/nbTot;
		int ratioP = 100*(nbDone-1)/nbTot;
		if(ratio != ratioP) System.out.println(ratio + "% done");
	}

	//round a double
	public static double round(double x, int decimalNB) {
		double pow = Math.pow(10, decimalNB);
		return ( (int)(x * pow + 0.5) ) / pow;
	}


	//get all files in a folder (recursivelly)
	public static ArrayList<File> getFiles(String folderPath) {
		return getFiles(new File(folderPath));
	}
	public static ArrayList<File> getFiles(File folder) {
		ArrayList<File> files = new ArrayList<File>();
		for (File file : folder.listFiles())
			if (file.isDirectory())
				files.addAll(getFiles(file));
			else
				files.add(file);
		return files;
	}


	//count file line number
	public static int fileLineCount(String inputFilePath){
		int i=0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(inputFilePath))));
			while (br.readLine() != null)
				i++;
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return i;
	}


	//clean string
	public static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

	public static String stripDiacritics(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
		return str;
	}

	public static String stripWeirdCaracters(String str) {
		String string = Normalizer.normalize(str, Normalizer.Form.NFD);
		return string.replaceAll("[^\\p{ASCII}]", "");
	}


	//PARIS into Paris
	public static String capitalizeOnlyFirstLetter(String s){
		String out = s.toLowerCase();
		return out.substring(0,1).toUpperCase() + out.substring(1);
	}



	//index list of hashmaps (only one value)
	public static HashMap<String, String> index(ArrayList<HashMap<String, String>> data, String indexKey, String valueCol) {
		HashMap<String, String> ind = new HashMap<String, String>();
		for(HashMap<String, String> elt : data)
			ind.put(elt.get(indexKey), elt.get(valueCol));
		return ind;
	}

	//index list of hashmaps (all values)
	public static HashMap<String,HashMap<String,String>> index(ArrayList<HashMap<String, String>> data, String indexKey) {
		HashMap<String,HashMap<String,String>> ind = new HashMap<String,HashMap<String,String>>();
		for(HashMap<String, String> elt : data)
			ind.put(elt.get(indexKey), elt);
		return ind;
	}

}
