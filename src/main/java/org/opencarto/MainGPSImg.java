package org.opencarto;

import java.awt.Color;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.datamodel.MultiScaleProperty;
import org.opencarto.datamodel.ZoomExtend;
import org.opencarto.datamodel.gps.GPSSegment;
import org.opencarto.datamodel.gps.GPSTrace;
import org.opencarto.io.GPSUtil;
import org.opencarto.processes.DefaultGeneralisation;
import org.opencarto.style.ColorScale;
import org.opencarto.style.Style;
import org.opencarto.style.basic.LineStyle;
import org.opencarto.style.gps.GPSSegmentSpeedStyle;
import org.opencarto.style.gps.GPSTraceDateStyle;
import org.opencarto.tiling.Tiling;
import org.opencarto.tiling.raster.RasterTileBuilder;
import org.opencarto.util.ColorUtil;

public class MainGPSImg {

	public static void main(String[] args) throws ParseException {
		String[] inPaths = new String[] {"/home/juju/GPS/strava/","/home/juju/GPS/gpx/"};
		//String[] inPaths = new String[] {"/home/juju/GPS/strava/"};
		//String[] inPaths = new String[] {"/home/juju/GPS/gpx_test/"};

		String outPath = "/home/juju/GPS/app_raster/gps_traces_raster/";
		ZoomExtend zs = new ZoomExtend(0,14);


		//styles based on traces

		if(false){

			//load traces
			ArrayList<GPSTrace> traces = new ArrayList<GPSTrace>();
			for(String inPath : inPaths){
				System.out.println("Load traces in "+inPath);
				File[] files = new File(inPath).listFiles();
				traces.addAll( GPSUtil.loadTraces(files) );
			}
			System.out.println(traces.size() + " traces loaded.");

			//make generalisation
			//new NoGeneralisation<GPSTrace>().perform(fs, zs);
			new DefaultGeneralisation<GPSTrace>(false).perform(traces, zs);



			//make tiles - default
			if(false){
				System.out.println("Tiling default");
				MultiScaleProperty<Style<GPSTrace>> style = new MultiScaleProperty<Style<GPSTrace>>()
						.set(new LineStyle<GPSTrace>().setWidth(1f).setColor(ColorUtil.RED), 0, 8)
						.set(new LineStyle<GPSTrace>().setWidth(0.8f).setColor(ColorUtil.RED), 9, 11)
						.set(new LineStyle<GPSTrace>().setWidth(0.6f).setColor(ColorUtil.RED), 12, 20)
						;
				new Tiling<GPSTrace>(traces, new RasterTileBuilder<GPSTrace>(style), outPath + "default/", zs, false).doTiling();
			}

			//make tiles - by date
			if(false){
				System.out.println("Tiling date");
				final long[] minmax = getMinMaxTime(traces);
				ColorScale<Long> colScale = new ColorScale<Long>(){
					//Color[] colRamp = ColorBrewer.Set1.getColorPalette(90);
					//Color[] colRamp = ColorBrewer.PRGn.getColorPalette(110);
					Color[] colRamp = ColorUtil.getColors(new Color[]{ColorUtil.BLUE, ColorUtil.YELLOW, ColorUtil.RED}, 250);
					public Color getColor(Long time) {
						return ColorUtil.getColor(colRamp, time, minmax[0], minmax[1]);
					}
				};
				MultiScaleProperty<Style<GPSTrace>> style = new MultiScaleProperty<Style<GPSTrace>>()
						.set(new GPSTraceDateStyle(colScale, 1.3f), 0, 20)
						;
				new Tiling<GPSTrace>(traces, new RasterTileBuilder<GPSTrace>(style), outPath + "date/", zs, false).doTiling();
			}
		}


		//Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded

		//styles based on segments

		if(true){
			//styles based on segments

			//load segments
			ArrayList<GPSSegment> segs = new ArrayList<GPSSegment>();
			for(String inPath : inPaths){
				System.out.println("Load segments in "+inPath);
				File[] files = new File(inPath).listFiles();
				segs.addAll( GPSUtil.loadSegments(files) );
			}
			System.out.println(segs.size() + " segments loaded.");



			//make tiles - by segment speed
			System.out.println("Tiling segment speed");

			ColorScale<Double> colScale = new ColorScale<Double>(){
				Color[] colRamp1 = ColorUtil.getColors(new Color[]{ColorUtil.GREEN, ColorUtil.BLUE}, 10);
				Color[] colRamp2 = ColorUtil.getColors(new Color[]{ColorUtil.BLUE, ColorUtil.PURPLE}, 10);
				Color[] colRamp3 = ColorUtil.getColors(new Color[]{ColorUtil.PURPLE, ColorUtil.YELLOW}, 10);
				public Color getColor(Double value) {
					if(value<30) return ColorUtil.getColor(colRamp1, value, 5, 30);
					if(value<140) return ColorUtil.getColor(colRamp2, value, 30, 140);
					return ColorUtil.getColor(colRamp3, value, 140, 350);
				}
			};
			MultiScaleProperty<Style<GPSSegment>> styleSpeed = new MultiScaleProperty<Style<GPSSegment>>()
					.set(new GPSSegmentSpeedStyle(colScale, 1.7f), 0, 20)
					;
			new Tiling<GPSSegment>(segs, new RasterTileBuilder<GPSSegment>(styleSpeed), outPath + "speed/", zs, false).doTiling();
		}

		/*
		Impossible to parse date: 2016-01-17T14:26:54.610Z
Impossible to parse date: 2016-01-16T11:22:19.750Z
Impossible to parse date: 2016-01-17T09:26:46.830Z
Impossible to parse date: 2016-01-17T15:51:49.650Z
Impossible to parse date: 2016-01-15T12:11:22.560Z
Impossible to parse date: 2016-01-16T13:15:43.610Z
Impossible to parse date: 2016-01-16T15:56:16.650Z
		 */

		System.out.println("Done.");
	}

	private static long[] getMinMaxTime(Collection<GPSTrace> traces){
		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
		for(GPSTrace trace : traces){
			if(trace.getStartTime() == null) continue;
			if(trace.getStartTime().getDate() == null) continue;
			long val = trace.getStartTime().getDate().getTime();
			if(val < min) min = val;
			if(val > max) max = val;
		}
		return new long[]{min,max};
	}









	//loading focusing on segments only
	/*
	//load gps segments from files
	public static ArrayList<Feature> loadSegments(File[] files) {
		System.out.println("Loading " + files.length + " files...");

		int nbTot = files.length;
		int nbDone = 1;

		ArrayList<Feature> segs = new ArrayList<Feature>();
		for (int i=0; i<files.length; i++) {
			segs.addAll( getSegments(files[i]) );
			Util.printProgress(nbDone++, nbTot);
		}
		return segs;
	}


	//load gps segments from file
	private static ArrayList<Feature> getSegments(File file) {
		//get file namespace
		String ns = XMLUtil.getFileNameSpace(file);

		//get segments from input files: tcx or gpx
		if("http://www.topografix.com/GPX/1/1".equals(ns))
			return getSegmentsGPX(file);

		if(ns == null || "".equals(ns))
			System.err.println("No namespace found in file: " + file.getName());
		else
			System.err.println("Unsupported format: " + ns + " for file: " + file.getName());
		return new ArrayList<Feature>();
	}

	private static ArrayList<Feature> getSegmentsGPX(File file) {
		ArrayList<Feature> segs = new ArrayList<Feature>();

		GeometryFactory gf = new GeometryFactory();
		DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'");

		//get the gpx object
		GpxType gpx = (GpxType) ((JAXBElement<?>)JAXBUtil.getUnmarshalledObject(file, GpxType.class)).getValue();

		//go through the traces (trk)
		for(TrkType trk : gpx.getTrk()) {
			//get all the points
			ArrayList<Object[]> points = new ArrayList<Object[]>();

			//go through the laps (trkSeg)
			for(TrksegType trkSeg : trk.getTrkseg()) {

				//go through the points (wpt)
				for(WptType wpt : trkSeg.getTrkpt()) {
					double lon = wpt.getLon().doubleValue();
					double lat = wpt.getLat().doubleValue();

					String timeString = null;
					if(wpt.getTime()!=null) timeString = wpt.getTime().toString();
					Date date = null;
					if(timeString != null)
						try { date = df1.parse(timeString);
						} catch (ParseException e) {
							try { date = df2.parse(timeString); }
							catch (ParseException e1) {
								//logger.error("Impossible to parse date: " + this.timeString);
								System.err.println("Impossible to parse date: " + timeString);
								date = null;
								//e1.printStackTrace();
							}
						}
					long time = date == null? 0 : date.getTime();

					points.add( new Object[]{new Coordinate(ProjectionUtil.getXGeo(lon), ProjectionUtil.getYGeo(lat)), time, lat} );
				}
			}

			//build segments from the points
			if (points.size() > 1){
				Object[] endPoint, startPoint = points.get(0);
				Coordinate startCoord, endCoord;
				double startLat,endLat, lengthM, s, duration;
				long startTime, endTime;
				for(int i=1; i<points.size(); i++) {
					endPoint = points.get(i);

					startCoord = (Coordinate)startPoint[0];
					endCoord = (Coordinate)endPoint[0];
					startLat = Double.parseDouble(startPoint[2].toString());
					long startTime, endTime;
					endLat = Double.parseDouble(endPoint[2].toString());

					Feature seg = new Feature();
					seg.setGeom( gf.createLineString( new Coordinate[] { startCoord, endCoord } ) );

					lengthM = startCoord.distance(endCoord)
	 * ProjectionUtil.getDeformationFactor( (startLat+endLat)*0.5 );		

					if(startPoint[1] == null || endPoint[1] == null) {
						startPoint = endPoint;
						continue;
					}

					startTime = Long.parseLong(startPoint[1].toString());
					endTime = Long.parseLong(endPoint[1].toString());
					duration = (endTime - startTime) * 0.001;

					s = 0;
					if(duration != 0 && startTime != 0 && endTime != 0)
						s = 3.6 * lengthM / duration;
					seg.getProperties().put("s", s);

					segs.add(seg);
					startPoint = endPoint;
				}
			}
			points.clear();
		}
		return segs;
	}
	 */
}
