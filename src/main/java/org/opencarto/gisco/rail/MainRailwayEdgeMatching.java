/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

import org.geotools.filter.text.cql2.CQL;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.opencarto.datamodel.Feature;
import org.opencarto.io.SHPUtil;
import org.opengis.filter.Filter;

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayEdgeMatching {

	public static void main(String[] args) throws Exception {

		//resolution data
		HashMap<String,Double> resolutions = new HashMap<String,Double>();
		resolutions.put("BE", 2.0);
		resolutions.put("LU", 1.5);
		resolutions.put("NL", 7.0);

		System.out.println("Load input tracks");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		Filter f = CQL.toFilter( "CNTR = 'BE' OR CNTR = 'LU' OR CNTR = 'NL'" );
		ArrayList<Feature> tracks = SHPUtil.loadSHP(basePath+"in/RailwayLink.shp", f).fs;
		System.out.println(tracks.size());

		System.out.println("Ensure input geometries are simple");
		for(Feature t : tracks) {
			MultiLineString mls = (MultiLineString) t.getGeom();
			if(mls.getNumGeometries() != 1) System.err.println("Input geometries should be simple linestrings");
			else t.setGeom( (LineString)mls.getGeometryN(0) );
		}

		System.out.println("Build spatial index");
		Quadtree si = new Quadtree();
		for(Feature t : tracks) si.insert(t.getGeom().getEnvelopeInternal(), t);

		System.out.println("Get list of countries ordered by resolution");
		ArrayList<String> cnts = new ArrayList<String>();
		cnts.addAll( resolutions.keySet() );
		cnts.sort(new Comparator<String>() {
			@Override
			public int compare(String cnt1, String ctn2) { return (int)(10000000 * (resolutions.get(ctn2) - resolutions.get(cnt1))); }
		});

		System.out.println("Set EM tag");
		for(Feature t : tracks) t.getProperties().put("EM", "No");

		System.out.println("Go through countries, starting with the one with the largest resolution");
		for(String cnt : cnts) {
			//get all tracks of the cnt
			ArrayList<Feature> tracksCnt = new ArrayList<Feature>();
			for(Feature t : tracks) if(t.getProperties().get("CNTR").equals(cnt)) tracksCnt.add(t);

			double res = resolutions.get(cnt);
			System.out.println(cnt + " - res=" + res + " - nb=" + tracksCnt.size());

			for(Feature t : tracksCnt) {

				//get all tracks that are 'nearby'
				Envelope env = t.getGeom().getEnvelopeInternal(); env.expandBy(res*1.01);

				for(Object t2 : si.query(env)) { //TODO order from longest to shortest?
					Feature t_ = (Feature) t2;
					if(t == t_) continue;
					if(t.getGeom().isEmpty()) continue;

					if(t_.getProperties().get("CNTR").equals(cnt)) continue;
					if(res < resolutions.get(t_.getProperties().get("CNTR"))) continue;
					if(! t_.getGeom().getEnvelopeInternal().intersects(env)) continue;
					//TODO add test on distance - should be below res*1.01

					//System.out.println( areConnected( (LineString)t.getGeom(), (LineString)t_.getGeom()) );

					if(areConnected( (LineString)t.getGeom(), (LineString)t_.getGeom())) continue;

					//tag the section
					t.getProperties().put("EM", "Involved");

					//remove t geometry part with t_
					Geometry buff = t_.getGeom().buffer(res);
					Geometry gDiff = t.getGeom().difference(buff);

					//if nothing left, remove it
					if(gDiff.isEmpty()) {
						si.remove(t.getGeom().getEnvelopeInternal(), t);
						t.setGeom(gDiff);
						tracks.remove(t);
						continue;
					}

					if(!(gDiff instanceof LineString)) continue; //TODO handle that case !

					//if t geometry has changed
					if(gDiff.getLength() != t.getGeom().getLength()) {
						//update geometry
						si.remove(t.getGeom().getEnvelopeInternal(), t);
						t.setGeom((LineString)gDiff);
						si.insert(t.getGeom().getEnvelopeInternal(), t);
						t.getProperties().put("EM", "changed");
					}

					//connect lines
					LineString tg_ = connectLineStrings( (LineString)t_.getGeom(), (LineString)t.getGeom(), 1.5*res );

					//nothing to connect
					if(tg_ == null) continue;

					//update t_ geometry
					si.remove(t_.getGeom().getEnvelopeInternal(), t_);
					t_.setGeom((LineString)tg_);
					si.insert(t_.getGeom().getEnvelopeInternal(), t_);
					t_.getProperties().put("EM", "changed");
				}
			}
		}

		System.out.println("Save output");
		System.out.println(tracks.size());
		SHPUtil.saveSHP(tracks, basePath+"out/EM/RailwayLinkEM.shp", SHPUtil.getCRS(basePath+"in/RailwayLink.shp"));

		System.out.println("End");
	}


	//connect ls1 to nearest point of ls2. Return the prolongates line of ls1 to nearest point of ls2.
	public static LineString connectLineStrings(LineString ls1, LineString ls2, double threshold) throws Exception {

		//TODO connect only from top

		//find points extrema and connect them from t_
		DistanceOp dop = new DistanceOp(ls1, ls2);

		if(dop.distance() > threshold)
			return null;

		Coordinate[] pts = dop.nearestPoints();
		if(pts.length != 2)
			throw new Exception("Unexpected number of points encountered (2 expected) in DistanceOp ("+pts.length+") around "+pts[0]);

		LineString comp = ls1.getFactory().createLineString(pts);
		LineMerger lm = new LineMerger();
		lm.add(ls1); lm.add(comp);
		Collection<?> lss = lm.getMergedLineStrings();
		if(lss.size() != 1) {
			System.err.println("Unexpected number of merged lines: nb="+lss.size()+" (expected value: 1).");
			return null;
		}
		Object out = lss.iterator().next();

		if(out instanceof LineString) return (LineString) out;
		if(out instanceof MultiLineString) {
			MultiLineString out_ = (MultiLineString) out;
			if(out_.getNumGeometries() != 1)
				throw new Exception("Unexpected number of geometries ("+out_.getNumGeometries()+" (expected value: 1).");
			return (LineString) out_.getGeometryN(0);
		}
		throw new Exception("Unexpected geometry type ("+out.getClass().getSimpleName()+". Linear geometry expected.");
	}

	private static boolean areConnected(LineString ls1, LineString ls2) {
		Coordinate[] cs1 = ls1.getCoordinates(), cs2 = ls2.getCoordinates();
		if(cs1[0].distance(cs2[0]) == 0) return true;
		if(cs1[0].distance(cs2[cs2.length-1]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[0]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[cs2.length-1]) == 0) return true;
		return false;
	}

}
