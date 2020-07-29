package org.opencarto.io;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBElement;

import org.opencarto.gps.datamodel.GPSPoint;
import org.opencarto.gps.datamodel.GPSSegment;
import org.opencarto.gps.datamodel.GPSTrace;
import org.opencarto.gps.datamodel.Lap;
import org.opencarto.io.bindings.gpx.v11.GpxType;
import org.opencarto.io.bindings.gpx.v11.TrkType;
import org.opencarto.io.bindings.gpx.v11.TrksegType;
import org.opencarto.io.bindings.gpx.v11.WptType;
import org.opencarto.io.bindings.tcx.v2.ActivityLapT;
import org.opencarto.io.bindings.tcx.v2.ActivityT;
import org.opencarto.io.bindings.tcx.v2.TrackT;
import org.opencarto.io.bindings.tcx.v2.TrackpointT;
import org.opencarto.io.bindings.tcx.v2.TrainingCenterDatabaseT;

import eu.europa.ec.eurostat.jgiscotools.util.Util;

public class GPSUtil {
	public static String FILE_NAME = "fileName";


	//load GPS traces from list of files
	public static ArrayList<GPSTrace> loadTraces(File[] files){
		System.out.println("Loading " + files.length + " files...");

		int nbTot = files.length;
		int nbDone = 1;

		ArrayList<GPSTrace> fs = new ArrayList<GPSTrace>();
		for (int i=0; i<files.length; i++) {
			fs.addAll( loadTraces(files[i]) );
			Util.printProgress(nbDone++, nbTot);
		}
		return fs;
	}

	//load GPS traces from file TODO merge with previous one with "..." argument
	public static ArrayList<GPSTrace> loadTraces(File file) {
		//get file namespace
		String ns = XMLUtil.getFileNameSpace(file);

		//get traces from input files
		if("http://www.topografix.com/GPX/1/1".equals(ns))
			return getTracesGPX(file);
		if("http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2".equals(ns))
			return getTracesTCX(file);

		if(ns == null || "".equals(ns))
			System.err.println("No namespace found in file: " + file.getName());
		else
			System.err.println("Unsupported format: " + ns + " for file: " + file.getName());
		return new ArrayList<GPSTrace>();
	}


	//load GPS traces from TCX file
	private static ArrayList<GPSTrace> getTracesTCX(File file) {
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


	//load GPS traces from GPX file
	private static ArrayList<GPSTrace> getTracesGPX(File file) {
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




	//loading focusing on segments only

	//load gps segments from files
	public static ArrayList<GPSSegment> loadSegments(File... files) {
		System.out.println("Loading " + files.length + " files...");

		int nbTot = files.length;
		int nbDone = 1;

		ArrayList<GPSSegment> segs = new ArrayList<GPSSegment>();
		for (int i=0; i<files.length; i++) {
			segs.addAll( getSegments(files[i]) );
			Util.printProgress(nbDone++, nbTot);
		}
		return segs;
	}


	//load gps segments from file
	private static ArrayList<GPSSegment> getSegments(File file) {
		//get file namespace
		String ns = XMLUtil.getFileNameSpace(file);

		//get segments from input files: tcx or gpx
		if("http://www.topografix.com/GPX/1/1".equals(ns))
			return getSegmentsGPX(file);
		//if("http://www.garmin.com/xmlschemas/TrainingCenterDatabase/v2".equals(ns))
		//	return getSegmentsTCX(file);

		if(ns == null || "".equals(ns))
			System.err.println("No namespace found in file: " + file.getName());
		else
			System.err.println("Unsupported format: " + ns + " for file: " + file.getName());
		return new ArrayList<GPSSegment>();

		/*ArrayList<GPSSegment> segs = new ArrayList<GPSSegment>();
		ArrayList<GPSTrace> traces = getTraces(file);
		for(GPSTrace trace : traces) segs.addAll(trace.getSegments());
		return segs;*/
	}

	private static ArrayList<GPSSegment> getSegmentsGPX(File file) {
		ArrayList<GPSSegment> segs = new ArrayList<GPSSegment>();

		//get the gpx object
		GpxType gpx = (GpxType) ((JAXBElement<?>)JAXBUtil.getUnmarshalledObject(file, GpxType.class)).getValue();

		//go through the traces (trk)
		for(TrkType trk : gpx.getTrk()) {
			//get all the points
			ArrayList<GPSPoint> points = new ArrayList<GPSPoint>();

			//go through the laps (trkSeg)
			for(TrksegType trkSeg : trk.getTrkseg()) {

				//go through the points (wpt)
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
			}

			//build segments from the points
			int nb = points.size();
			if (nb > 1){
				GPSPoint startPoint = points.get(0);
				GPSPoint endPoint;
				for(int i=1; i<nb; i++) {
					endPoint = points.get(i);
					segs.add( new GPSSegment(startPoint, endPoint) );
					startPoint = endPoint;
				}
			}
		}
		return segs;
	}

}
