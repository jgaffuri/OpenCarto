package org.opencarto.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opencarto.datamodel.statistics.Stat;
import org.opencarto.datamodel.statistics.StatsHypercube;

/**
 * @author julien Gaffuri
 *
 */
public class TSVUtil {

	public static StatsHypercube load(String inputFilePath){
		ArrayList<File> files = new ArrayList<File>();
		files.add(new File(inputFilePath));
		return load(files);
	}

	public static StatsHypercube load(ArrayList<File> files){
		if(files==null || files.size()==0) return null;
		String sep="\t";
		BufferedReader br = null;
		StatsHypercube sh = new StatsHypercube();
		try {
			//read dimensions in first file
			br = new BufferedReader(new FileReader(files.get(0)));

			//read first line
			String line = br.readLine();
			StringTokenizer st = new StringTokenizer(line,sep);

			//read dim names
			StringTokenizer stDims = new StringTokenizer(st.nextToken(),",");
			while(stDims.hasMoreTokens())
				sh.dimLabels.add( stDims.nextToken().replace("\\time", "") );
			sh.dimLabels.add("time");
			br.close();



			//read the files

			for(File f:files){
				br = new BufferedReader(new FileReader(f));

				//read the years
				line = br.readLine();
				st = new StringTokenizer(line,sep);
				st.nextToken();
				String[] years=new String[st.countTokens()];
				int i=0;
				while(st.hasMoreTokens())
					years[i++] = st.nextToken();

				while ((line = br.readLine()) != null) {
					st = new StringTokenizer(line,sep);

					//read dims
					String lbl = st.nextToken();

					int yearIndex=-1;
					while(st.hasMoreTokens()){
						yearIndex++;
						String val_ = st.nextToken().replace(" ", "").replace("a", "").replace("s", "").replace("b", "").replace("c", "").replace("d", "").replace("e", "").replace("f", "").replace("p", "").replace("u", "").replace("z", "").replace("i", "").replace("n", "").replace("r", "");
						if(":".equals(val_)) continue;

						Stat s = new Stat();
						//dims
						stDims = new StringTokenizer(lbl,","); int dimIndex=0;
						while(stDims.hasMoreTokens()){
							String dimLabel = sh.dimLabels.get(dimIndex++);
							String dimValue = stDims.nextToken();
							s.dims.put(dimLabel, dimValue);
						}
						//year
						s.dims.put("time", years[yearIndex]);
						//value
						s.value = Double.parseDouble(val_);

						sh.stats.add(s);
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
		return sh;
	}

}
