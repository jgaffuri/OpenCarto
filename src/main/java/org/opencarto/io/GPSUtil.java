package org.opencarto.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opencarto.datamodel.gps.GPSPoint;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.datamodel.gps.Lap;
import org.opencarto.io.bindings.gpx.v11.GpxType;
import org.opencarto.io.bindings.gpx.v11.TrkType;
import org.opencarto.io.bindings.gpx.v11.TrksegType;
import org.opencarto.io.bindings.gpx.v11.WptType;
import org.opencarto.io.bindings.tcx.v2.ActivityLapT;
import org.opencarto.io.bindings.tcx.v2.ActivityT;
import org.opencarto.io.bindings.tcx.v2.TrackT;
import org.opencarto.io.bindings.tcx.v2.TrackpointT;
import org.opencarto.io.bindings.tcx.v2.TrainingCenterDatabaseT;
import org.opencarto.util.Util;
import org.w3c.dom.Document;

public class GPSUtil {
	public static String FILE_NAME = "fileName";


	//load GPS data from list of files
	public static ArrayList<GPSTrace> load(File[] files){
		System.out.println("Loading " + files.length + " files...");

		int nbTot = files.length;
		int nbDone = 1;

		ArrayList<GPSTrace> fs = new ArrayList<GPSTrace>();
		for (int i=0; i<files.length; i++) {
			fs.addAll( getTraces(files[i]) );
			Util.printProgress(nbDone++, nbTot);
		}
		return fs;
	}


	//load GPS data from file
	public static ArrayList<GPSTrace> getTraces(File file) {
		ArrayList<GPSTrace> traces = new ArrayList<GPSTrace>();

		//get file namespace
		String ns = null;
		try {
			FileInputStream ips = new FileInputStream(file);
			Document XMLDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( ips );
			ns = XMLDoc.getDocumentElement().getAttribute("xmlns");
			try { ips.close(); } catch (IOException e) { e.printStackTrace(); }
		} catch (Exception e) {
			e.printStackTrace();
		}

		//get traces from input files: tcx of gpx
		if("http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2".equals(ns))
			traces = openTCX(file);
		else if("http://www.topografix.com/GPX/1/1".equals(ns))
			traces = openGPX(file);
		else {
			if(ns == null || "".equals(ns))
				System.err.println("No namespace found in file: " + file.getName());
			else
				System.err.println("Unsupported format: " + ns);
		}
		return traces;
	}


	//load GPS data from TCX file
	private static ArrayList<GPSTrace> openTCX(File file) {
		ArrayList<GPSTrace> traces = new ArrayList<GPSTrace>();

		//get the tcx object
		TrainingCenterDatabaseT tcx = (TrainingCenterDatabaseT) ((JAXBElement<?>)JAXBUtil.getUnmarshalledObject(file, TrainingCenterDatabaseT.class)).getValue();

		//go through the activities
		for(ActivityT activity : tcx.getActivities().getActivity()) {

			//get the laps of the trace
			ArrayList<Lap> laps = new ArrayList<Lap>();
			for(ActivityLapT lap : activity.getLap()) {

				//get the tracks of the lap
				for(TrackT track : lap.getTrack()) {

					//get the points of the track
					ArrayList<GPSPoint> points = new ArrayList<GPSPoint>();
					for(TrackpointT pt : track.getTrackpoint()) {

						if(pt.getPosition() == null) continue;

						double lat = pt.getPosition().getLatitudeDegrees();
						double lon = pt.getPosition().getLongitudeDegrees();

						double altitude = 0;
						if(pt.getAltitudeMeters() != null) altitude = pt.getAltitudeMeters().doubleValue();

						String timeStamp = pt.getTime().toString();

						GPSPoint pt2 = new GPSPoint(lat, lon, altitude, timeStamp);
						points.add( pt2 );
					}
					laps.add(new Lap(points));
				}
			}
			traces.add(new GPSTrace(laps));
		}
		return traces;
	}


	//load GPS data from GPX file
	private static ArrayList<GPSTrace> openGPX(File file) {
		ArrayList<GPSTrace> traces = new ArrayList<GPSTrace>();

		//get the gpx object
		GpxType gpx = (GpxType) ((JAXBElement<?>)JAXBUtil.getUnmarshalledObject(file, GpxType.class)).getValue();

		//go through the traces (trk)
		for(TrkType trk : gpx.getTrk()) {

			//go through the laps (trkSeg)
			ArrayList<Lap> laps = new ArrayList<Lap>();
			for(TrksegType trkSeg : trk.getTrkseg()) {

				//go through the points (wpt)
				ArrayList<GPSPoint> points = new ArrayList<GPSPoint>();
				for(WptType wpt : trkSeg.getTrkpt()) {
					double lat = wpt.getLat().doubleValue();
					double lon = wpt.getLon().doubleValue();

					double altitude = 0;
					if(wpt.getEle()!=null) altitude = wpt.getEle().doubleValue();

					String timeStamp = null;
					if(wpt.getTime()!=null) timeStamp = wpt.getTime().toString();

					GPSPoint pt = new GPSPoint(lat, lon, altitude, timeStamp);
					points.add( pt );
				}
				laps.add(new Lap(points));
			}
			traces.add(new GPSTrace(laps));
		}
		return traces;
	}

}
