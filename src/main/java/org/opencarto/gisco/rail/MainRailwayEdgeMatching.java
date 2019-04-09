/**
 * 
 */
package org.opencarto.gisco.rail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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

/**
 * @author julien Gaffuri
 *
 */
public class MainRailwayEdgeMatching {

	public static void main(String[] args) throws Exception {
		boolean b;

		//TODO country clip, with very large buffer - depending on country dataset resolution + railway resolution
		//objective is to exclude lines really far away (PT)

		//TODO: get OSM, for comparision
		//TODO: improve input file: projection 3035, features with null geometries for IT and RO. Multi geoms for IT. fix DK. get attributes. get more countries. get better resolution.

		//TODO: when section is similar to another one (min dist + hausdorf small), remove the one which is not from the country.
		//TODO: remove sections not in their countries, far and not connected (?)
		//TODO: decompose sections with extreme points of sections of other countries. Then link them. Remove similar sections (joining same nodes) from diff countries.

		//resolution data
		HashMap<String,Double> resolutions = new HashMap<String,Double>();
		resolutions.put("BE", 0.8);
		resolutions.put("LU", 1.2);
		resolutions.put("AT", 1.3);
		resolutions.put("NL", 5.0);
		resolutions.put("NO", 5.1);
		resolutions.put("CH", 6.0);
		resolutions.put("SE", 6.1);
		resolutions.put("FR", 7.9);
		resolutions.put("ES", 8.0);
		resolutions.put("FI", 8.1);
		resolutions.put("UK", 8.4);
		resolutions.put("IT", 14.0);
		resolutions.put("PL", 25.0);
		resolutions.put("DE", 40.0);
		resolutions.put("IE", 70.0);
		resolutions.put("PT", 250.0);

		//TODO get real values
		resolutions.put("RO", 250.0);
		resolutions.put("EL", 250.0);
		resolutions.put("DK", 250.0);
		resolutions.put("EE", 250.0);


		System.out.println("Load input sections");
		String basePath = "/home/juju/Bureau/gisco_rail/";
		//Filter fil = CQL.toFilter( "CNTR <> 'RO' AND CNTR <> 'EL' AND CNTR <> 'DK' AND CNTR <> 'EE'" );
		ArrayList<Feature> secs = SHPUtil.loadSHP(basePath+"in/RailwayLinkClean.shp").fs;
		System.out.println(secs.size());

		System.out.println("Check if input geometries are simple");
		for(Feature s : secs) {
			MultiLineString mls = (MultiLineString) s.getGeom();
			if(mls.getNumGeometries() != 1)
				System.err.println("Input geometries should be simple linestrings. nb=" + mls.getNumGeometries() + " CNTR=" + s.getProperties().get("CNTR"));
			s.setGeom( (LineString)mls.getGeometryN(0) );
		}

		System.out.println("Build spatial index");
		Quadtree si = new Quadtree();
		for(Feature c : secs) si.insert(c.getGeom().getEnvelopeInternal(), c);

		System.out.println("Get list of countries ordered by resolution");
		ArrayList<String> cnts = new ArrayList<String>();
		cnts.addAll( resolutions.keySet() );
		cnts.sort(new Comparator<String>() {
			@Override
			public int compare(String cnt1, String ctn2) { return (int)(10000000 * (resolutions.get(ctn2) - resolutions.get(cnt1))); }
		});
		System.out.println(cnts);

		System.out.println("Get worst resolution value");
		double resMax = Collections.max(resolutions.values());

		System.out.println("Initialise EM tag");
		for(Feature s : secs) s.getProperties().put("EM", "no");



		//TODO correct cnts order !!!
		System.out.println("Clip with buffer difference of all sections, depending on country resolution");
		ArrayList<Feature> secsOut = new ArrayList<Feature>();
		for(Feature s : secs) {
			if(s.getGeom().isEmpty()) continue;

			String cnt = s.getProperties().get("CNTR").toString();
			double res = resolutions.get(cnt);
			Envelope env = s.getGeom().getEnvelopeInternal(); env.expandBy(resMax*1.01);

			//s to be 'cut' by sections from other countries with better resolution
			Geometry g = s.getGeom();
			boolean changed = false;
			for(Object s2 : si.query(env)) {
				Feature s_ = (Feature) s2;

				//filter
				if(s == s_) continue;
				if(! s_.getGeom().getEnvelopeInternal().intersects(env)) continue;
				if(s_.getGeom().isEmpty()) continue;
				String cnt_ = s_.getProperties().get("CNTR").toString();
				if(cnt_.equals(cnt)) continue;
				double res_ = resolutions.get(cnt_);
				if(res_ > res) continue; //s to be cut by those with better resolution
				if(areConnected( (LineString)s.getGeom(), (LineString)s_.getGeom())) continue;

				//tag
				if(!s_.getProperties().get("EM").equals("changed")) s_.getProperties().put("EM", "involved");

				g = g.difference( s_.getGeom().buffer(res) );
				changed=true;
				if(g.isEmpty()) break;
			}

			if(!changed) {
				secsOut.add(s);
				continue;
			}
			if(g.isEmpty()) {
				si.remove(s.getGeom().getEnvelopeInternal(), s);
				s.setGeom(g);
				continue;
			}
			if(g instanceof LineString) {
				si.remove(s.getGeom().getEnvelopeInternal(), s);
				s.setGeom(g);
				si.insert(s.getGeom().getEnvelopeInternal(), s);
				s.getProperties().put("EM", "changed");
				secsOut.add(s);
			} else {
				s.getProperties().put("EM", "changed");
				si.remove(s.getGeom().getEnvelopeInternal(), s);
				s.setGeom(g.getFactory().createLineString());
				MultiLineString mls = (MultiLineString)g;
				for(int i=0; i<mls.getNumGeometries(); i++) {
					Feature f = new Feature();
					f.setGeom((LineString) mls.getGeometryN(i));
					f.getProperties().putAll(s.getProperties());
					secsOut.add(f);
					si.insert(f.getGeom().getEnvelopeInternal(), f);
				}
			}

		}
		secs.clear(); secs = secsOut;


/*
		System.out.println("Handle easy cases");
		//Go through countries, starting with the one with the most detailled resolution
		for(String cnt : cnts) {
			//get all sections of the cnt
			ArrayList<Feature> secCnt = new ArrayList<Feature>();
			for(Feature s : secs) if(s.getProperties().get("CNTR").equals(cnt)) secCnt.add(s);

			double res = resolutions.get(cnt);
			System.out.println(cnt + " - res=" + res + " - nb=" + secCnt.size());

			for(Feature s : secCnt) {

				if(s.getGeom().isEmpty()) continue;
				Envelope env = s.getGeom().getEnvelopeInternal(); env.expandBy(resMax*1.01);

				//get all sections that are potential candidates for matching
				ArrayList<Feature> secs_ = new ArrayList<Feature>();
				for(Object s2 : si.query(env)) {
					Feature s_ = (Feature) s2;

					//filter
					if(s == s_) continue;
					if(s_.getGeom().isEmpty()) continue;
					String cnt_ = s_.getProperties().get("CNTR").toString();
					double res_ = resolutions.get(cnt_);
					if(cnt_.equals(cnt)) continue;
					if(areConnected( (LineString)s.getGeom(), (LineString)s_.getGeom())) continue;
					if(! s_.getGeom().getEnvelopeInternal().intersects(env)) continue;
					if(s.getGeom().distance(s_.getGeom()) > Math.max(res, res_)*1.01) continue;

					secs_.add(s_);
				}

				if(secs_.size()==0) continue;

				//1-1 case
				if(secs_.size() == 1) {
					Feature s_ = secs_.iterator().next();
					String cnt_ = s_.getProperties().get("CNTR").toString();
					double res_ = resolutions.get(cnt_);
					LineString ls = (LineString) s.getGeom(), ls_ = (LineString) s_.getGeom();

					//TODO
					//switch s and s_ to ensure s_ has the largest resolution
					//if(res>res_) {
					//	Feature aux=s; s=s_; s_=aux;
					//	String aux___=cnt; cnt=cnt_; cnt_=aux___;
					//	double aux__=res; res=res_; res_=aux__;
					//	LineString aux_=ls; ls=ls_; ls_=aux_;
					//}

					//compute distance between both sections
					DistanceOp dop = new DistanceOp(ls, ls_);
					Coordinate[] pts = dop.nearestPoints();

					//case when minimum distance is reached at the tip of both sections: simply extend the section with the largest resolution
					if( ( pts[0].distance(ls.getCoordinateN(0)) == 0 || pts[0].distance(ls.getCoordinateN(ls.getCoordinates().length-1)) == 0 ) &&
							( pts[1].distance(ls_.getCoordinateN(0)) == 0 || pts[1].distance(ls_.getCoordinateN(ls_.getCoordinates().length-1)) == 0 ) ) {

						//try to shorten a bit ls_
						Geometry ls__ = ls_.difference( ls.buffer(res_) );
						if(!ls__.isEmpty() && ls__ instanceof LineString) {
							ls_ = (LineString) ls__;
							pts = new DistanceOp(ls, ls_).nearestPoints();
						}

						//set new geometry
						ls_ = connectLineStrings(ls_, pts);
						b = si.remove(s_.getGeom().getEnvelopeInternal(), s_); if(!b) System.err.println("Error when removing section from spatial index");
						s_.setGeom(ls_);
						si.insert(s_.getGeom().getEnvelopeInternal(), s_);

						//tag
						s_.getProperties().put("EM", "changed");
						if(!s.getProperties().get("EM").equals("changed")) s.getProperties().put("EM", "involved");

						continue;
					}

					//case where sections intersect
					//if(dop.distance() == 0) {
					//
					//System.out.println("Intersection near " + pts[0] );
					//}


					//compute minimum distance and hausdorf distance
					//do buffer stuff?

				}

				//1-2 case
				if(secs_.size() == 2) {
					//compare minimum distance of the two - check if comparable

					continue;
				}

			}
		}
*/



		/*
		System.out.println("Go through countries, starting with the one with the most detailled resolution");
		for(String cnt : cnts) {
			//get all sections of the cnt
			ArrayList<Feature> secCnt = new ArrayList<Feature>();
			for(Feature s : secs) if(s.getProperties().get("CNTR").equals(cnt)) secCnt.add(s);

			double res = resolutions.get(cnt);
			System.out.println(cnt + " - res=" + res + " - nb=" + secCnt.size());

			for(Feature s : secCnt) {

				//get all sections that are 'nearby'
				Envelope env = s.getGeom().getEnvelopeInternal(); env.expandBy(res*1.01);

				List<?> secs_ = si.query(env); //TODO order?
				for(Object s2 : secs_) {
					Feature s_ = (Feature) s2;
					if(s == s_) continue;
					if(s.getGeom().isEmpty()) continue;

					if(s_.getProperties().get("CNTR").equals(cnt)) continue;
					if(res < resolutions.get(s_.getProperties().get("CNTR"))) continue;
					if(areConnected( (LineString)s.getGeom(), (LineString)s_.getGeom())) continue;
					if(! s_.getGeom().getEnvelopeInternal().intersects(env)) continue;
					if(s.getGeom().distance(s_.getGeom()) > res*1.01) continue;

					//tag the section
					s.getProperties().put("EM", "involved");

					//remove t geometry part with t_
					Geometry buff = s_.getGeom().buffer(res);
					Geometry gDiff = s.getGeom().difference(buff);

					//if nothing left, remove it
					if(gDiff.isEmpty()) {
						si.remove(s.getGeom().getEnvelopeInternal(), s);
						s.setGeom(gDiff);
						secs.remove(s);
						continue;
					}

					if(!(gDiff instanceof LineString)) continue; //TODO handle that case !

					//if t geometry has changed
					if(gDiff.getLength() != s.getGeom().getLength()) {
						//update geometry
						si.remove(s.getGeom().getEnvelopeInternal(), s);
						s.setGeom((LineString)gDiff);
						si.insert(s.getGeom().getEnvelopeInternal(), s);
						s.getProperties().put("EM", "changed");
					}

					//connect lines
					//TODO when tg_ geom is changed, former connections should be preserved! !
					LineString tg_ = connectLineStringsTip( (LineString)s_.getGeom(), (LineString)s.getGeom(), 1.5*res );

					//nothing to connect
					if(tg_ == null) continue;

					//update t_ geometry
					si.remove(s_.getGeom().getEnvelopeInternal(), s_);
					s_.setGeom((LineString)tg_);
					si.insert(s_.getGeom().getEnvelopeInternal(), s_);
					s_.getProperties().put("EM", "changed");
				}
			}
		}
		 */

		System.out.println("Save output " + secs.size());
		SHPUtil.saveSHP(secs, basePath+"out/EM/RailwayLinkEM.shp", SHPUtil.getCRS(basePath+"in/RailwayLink.shp"));

		System.out.println("End");
	}


	//connect ls1 to nearest point of ls2. Return the prolongates line of ls1 to nearest point of ls2.
	public static LineString connectLineStringsTip(LineString ls1, LineString ls2, double threshold) throws Exception {

		//find points extrema and connect them from t_
		DistanceOp dop = new DistanceOp(ls1, ls2);

		if(dop.distance() > threshold)
			return null;

		Coordinate[] pts = dop.nearestPoints();
		if(pts.length != 2)
			throw new Exception("Unexpected number of points encountered (2 expected) in DistanceOp ("+pts.length+") around "+pts[0]);

		//connect only from tip: check both coordinates are from extreme points
		if( pts[0].distance(ls1.getCoordinateN(0)) != 0 && pts[0].distance(ls1.getCoordinateN(ls1.getCoordinates().length-1)) != 0 ) return null;
		if( pts[1].distance(ls2.getCoordinateN(0)) != 0 && pts[1].distance(ls2.getCoordinateN(ls2.getCoordinates().length-1)) != 0 ) return null;

		return connectLineStrings(ls1, pts);
	}



	//connect ls1 to ls2. Return the prolongates line of ls1. pts are the points returned by DistanceOp.nearestPoints()
	public static LineString connectLineStrings(LineString ls1, Coordinate[] pts) throws Exception {
		LineString comp = ls1.getFactory().createLineString(pts);
		LineMerger lm = new LineMerger();
		lm.add(ls1); lm.add(comp);
		Collection<?> lss = lm.getMergedLineStrings();
		if(lss.size() != 1) {
			System.err.println("Unexpected number of merged lines: "+lss.size()+" (expected value: 1).");
			for(Object l : lss) System.err.println(l);
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




	//check if two linestrings are already connected from their tips
	private static boolean areConnected(LineString ls1, LineString ls2) {
		Coordinate[] cs1 = ls1.getCoordinates(), cs2 = ls2.getCoordinates();
		if(cs1[0].distance(cs2[0]) == 0) return true;
		if(cs1[0].distance(cs2[cs2.length-1]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[0]) == 0) return true;
		if(cs1[cs1.length-1].distance(cs2[cs2.length-1]) == 0) return true;
		return false;
	}

}
