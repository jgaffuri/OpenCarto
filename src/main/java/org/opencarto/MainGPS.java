package org.opencarto;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSTime;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.GPSGeneralisation;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.description.Description;
import org.opencarto.tiling.description.DescriptionBuilder;
import org.opencarto.tiling.vector.VectorTileBuilder;
import org.opencarto.util.Util;

public class MainGPS {
	static String outPath_;

	public static void main(String[] args) {
		String inPath = "/home/juju/GPS/gpx_test2/";
		String outPath = "/home/juju/workspace/opencarto-code/client/war/data/GPS_tiles/";
		int zoomMax = 13;

		if(args.length == 3){
			inPath = args[0];
			outPath = args[1];
			zoomMax = Integer.parseInt(args[2]);
		}

		outPath_ = outPath;
		ZoomExtend zs = new ZoomExtend(0,zoomMax);

		//load traces
		System.out.println("Load traces in "+inPath);
		File[] files = new File(inPath).listFiles();
		ArrayList<GPSTrace> fs = GPSUtil.load(files);
		System.out.println(fs.size() + " traces loaded.");

		//make generalisation
		new GPSGeneralisation().perform(fs, zs);

		//make tiles
		System.out.println("Tiling");
		new Tiling(fs, new VectorTileBuilder(), outPath, zs, false).doTiling();

		System.out.println("Descriptions");
		Description.export(fs, outPath, new MainGPS().new TraceDescriptionBuilder(), false);

		System.out.println("Done.");
	}


	class TraceDescriptionBuilder implements DescriptionBuilder {
		public String getDescription(Feature f) {
			GPSTrace t = (GPSTrace)f;

			if(t.getComponents().size()!=0){
				//compute stats
				double dist = 0;
				GPSTime tMin=null, tMax=null;
				for(Feature f2:t.getComponents()){
					GPSTrace t_ = (GPSTrace)f2;
					dist += t_.getLengthM();
					if(tMin==null) tMin=t_.getStartTime();
					if(tMax==null) tMax=t_.getEndTime();
					if(t_.getStartTime()!=null && t_.getStartTime().compareTo(tMin)<0) tMin=t_.getStartTime();
					if(t_.getEndTime()!=null && t_.getEndTime().compareTo(tMax)>0) tMax=t_.getEndTime();
				}

				String html = "<b>" + t.getComponents().size() + " traces here</b><br>";
				html += Util.round(0.001*dist,1) + "km";
				if(tMin != null && tMax != null){
					html += ", from "+tMin.getNiceDate()+" to " +tMax.getNiceDate()+ "<br>";
				}
				html += "Zoom-in to see more.<br>";
				return html;
				//make drawing?
			}

			String desc = "";
			if(t.getStartTime() != null && t.getEndTime() != null){
				//title
				desc += "<b>"+new SimpleDateFormat("EEEE", Locale.ENGLISH).format(t.getStartTime().getDate())+" "+t.getStartTime().getNiceDate()+"</b><br>";
				//start and end dates
				desc += "from "+t.getStartTime().getHM()+" to "+t.getEndTime().getHM()+"<br>";
				//length
				desc += Util.round(0.001*t.getLengthM(),1)+"km in ";
				//duration
				int h = (int)(t.getDurationS()/3600);
				if(h!=0) desc += h+"h ";
				int m = (int)((t.getDurationS()-3600*h)/60);
				if(m!=0) desc += m+"min ";
				int s = (int) (t.getDurationS())-3600*h-60*m;
				if(s!=0) desc += s+"s";
				desc +=  "<br>";
				//speed
				desc += "<b>Speed</b><br>"+Util.round(t.getMeanSpeedKmH(),1)+"km/h in average<br>"
						+ "Between " + Util.round(t.getMinSpeedKmH(),1) + " and "+Util.round(t.getMaxSpeedKmH(),1)+"km/h<br>"
						+ "<img src=\""+"http://ahahah.eu/data/geo/tiles_gps/p/"+f.id+"_profile_speed_dist.png"+"\" alt=\"Speed profile in distance\">"
						+ "<br>";
			} else {
				//length
				desc += "<b>"+Util.round(0.001*t.getLengthM(),1)+"km"+"</b><br>";
			}
			//elevation
			desc += "<b>Elevation</b><br>"
					+ "Between " + Util.round(t.getMinGPSElevationSegments(),1) + " and " + Util.round(t.getMaxGPSElevationSegments(),1)+"m<br>"
					+ "<img src=\""+"http://ahahah.eu/data/geo/tiles_gps/p/"+f.id+"_profile_ele_dist.png"+"\" alt=\"Elevation profile in distance\">"
					;

			desc = "<html>"+desc+"</html>";

			try {
				int wt=200,ht=50;
				ImageIO.write(t.getSpeedProfileInDistance(wt, ht), "png", new File(outPath_+"p/"+f.id+"_profile_speed_dist.png"));
				ImageIO.write(t.getElevationProfileInDistance(wt, ht), "png", new File(outPath_+"p/"+f.id+"_profile_ele_dist.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			return desc;
		}
	}
}
