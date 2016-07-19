package org.opencarto.algo.partition;

import java.util.ArrayList;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opencarto.algo.base.DouglasPeuckerRamerFilter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

/**
 * @author julien Gaffuri
 *
 */
public class PartitionGeneralisation {

	//A partition link: line to be simplified. It is linked to its two faces.
	private static class PartitionLink{
		LineString line;
		ArrayList<PartitionFace> faces = new ArrayList<PartitionFace>();
		public PartitionLink(LineString line) { this.line = line; }
	}

	//A partition face. It is linked to a feature and links around it.
	private static class PartitionFace{
		SimpleFeature f;
		ArrayList<PartitionLink> links = new ArrayList<PartitionLink>();
		public PartitionFace(SimpleFeature f) { this.f = f; }
	}

	@SuppressWarnings("unchecked")
	public static void generalisePartition(FeatureCollection<SimpleFeatureType,SimpleFeature> fs, String geomAtt, double dp) {

		System.out.println("Get polygon outlines");
		ArrayList<LineString> lss = new ArrayList<LineString>();
		FeatureIterator<SimpleFeature> it = fs.features();
		try {
			while( it.hasNext() ){
				SimpleFeature f=it.next();
				Geometry geom = (Geometry)f.getAttribute(geomAtt);
				for(int j=0; j<geom.getNumGeometries(); j++){
					Polygon p = (Polygon) geom.getGeometryN(j).buffer(0);
					lss.add(p.getExteriorRing());
					for(int i=0; i<p.getNumInteriorRing(); i++)
						lss.add(p.getInteriorRingN(i));
				}
			}
		}
		finally {
			it.close();
		}

		System.out.println("Union polygon outlines");
		MultiLineString mls = new GeometryFactory().createMultiLineString( lss.toArray(new LineString[lss.size()]) );
		lss.clear();
		mls = (MultiLineString) mls.union();
		for(int i=0; i<mls.getNumGeometries(); i++)
			lss.add((LineString) mls.getGeometryN(i));
		mls = null;

		System.out.println("Apply line merger");
		LineMerger lm = new LineMerger();
		lm.add(lss);
		lss = (ArrayList<LineString>) lm.getMergedLineStrings();
		lm = null;

		System.out.println("Build the links");
		ArrayList<PartitionLink> links = new ArrayList<PartitionLink>();
		for(LineString ls : lss)
			links.add(new PartitionLink(ls));
		lss.clear();

		System.out.println("Build the faces");
		ArrayList<PartitionFace> faces = new ArrayList<PartitionFace>();
		it = fs.features();
		try {
			while( it.hasNext() ){
				SimpleFeature f=it.next();
				faces.add(new PartitionFace(f));
			}
		}
		finally {
			it.close();
		}

		System.out.println("Build link/face topology");
		for(PartitionLink link : links){
			//get the faces touching
			ArrayList<PartitionFace> facesToLink = new ArrayList<PartitionFace>();
			for(PartitionFace face : faces){
				Geometry geom = (Geometry)face.f.getAttribute(geomAtt);
				if(!geom.getEnvelopeInternal().intersects(link.line.getEnvelopeInternal())) continue;
				Geometry inter = geom.intersection(link.line);
				if(inter==null) continue;
				if(inter.isEmpty()) continue;
				if(inter.getArea() != 0) {
					System.err.println("Error in partitionning: Area should be null. " + inter.getArea());
					continue;
				}
				if(inter.getLength() == 0) continue;
				//rep to be linked to ls
				facesToLink.add(face);
			}

			if(facesToLink.size() == 0 || facesToLink.size()>2) {
				System.err.println("Error in partitionning: bad number of faces to link. " + facesToLink.size());
				continue;
			}

			//link
			link.faces.addAll(facesToLink);
			for(PartitionFace face : facesToLink) face.links.add(link);
		}

		System.out.println("Simplify the links");
		for(PartitionLink link : links)
			simplify(link, geomAtt, dp);
	}


	private static boolean simplify(PartitionLink link, String geomAtt, double res) {
		//simplify link with DPR
		LineString lineS = (LineString) DouglasPeuckerRamerFilter.get(link.line,2*res);

		//try to rebuild face geometries
		if(link.faces.size() == 1){
			PartitionFace face = link.faces.get(0);
			Geometry geom = rebuildFace(face, link, lineS, geomAtt);
			if(geom==null || geom.isEmpty() || !geom.isValid()) return false;

			link.line = lineS;
			face.f.setAttribute(geomAtt, geom);
		} else if(link.faces.size() == 2){
			PartitionFace face0 = link.faces.get(0);
			Geometry geom0 = rebuildFace(face0, link, lineS, geomAtt);
			if(geom0==null || geom0.isEmpty() || !geom0.isValid()) return false;

			PartitionFace face1 = link.faces.get(1);
			Geometry geom1 = rebuildFace(face1, link, lineS, geomAtt);
			if(geom1==null || geom1.isEmpty() || !geom1.isValid()) return false;

			link.line = lineS;
			face0.f.setAttribute(geomAtt, geom0);
			face1.f.setAttribute(geomAtt, geom1);
		} else {
			System.err.println("Bad face number (should be 1 or 2): " + link.faces.size());
			return false;
		}
		return true;
	}


	@SuppressWarnings("unchecked")
	private static Geometry rebuildFace(PartitionFace face, PartitionLink link, LineString lineS, String geomAtt) {
		GeometryFactory gf = new GeometryFactory();

		Geometry geom = (Geometry)face.f.getAttribute(geomAtt);
		if(geom instanceof Polygon){

			//get all the lines
			ArrayList<LineString> lss = new ArrayList<LineString>();
			for(PartitionLink link_ : face.links){
				if(link_ == link)
					lss.add(lineS);
				else
					lss.add(link_.line);
			}

			//merge the lines
			LineMerger lm = new LineMerger();
			lm.add(lss);
			lss = (ArrayList<LineString>) lm.getMergedLineStrings();
			lm = null;

			//get external ring (the ring with the maximum area of the envelope)
			LineString shell_ = null;
			double maxA = -1;
			for(LineString ls : lss){
				double a = ls.getEnvelopeInternal().getArea();
				if(a>maxA){
					maxA=a;
					shell_ = ls;
				}
			}
			Coordinate[] cs = shell_.getCoordinates();
			if(cs.length<4) return null;
			LinearRing shell = gf.createLinearRing(cs);

			//get the rings
			LinearRing[] holes = new LinearRing[lss.size()-1];
			int i=0;
			for(LineString ls : lss){
				if(ls==shell_) continue;
				cs = ls.getCoordinates();
				if(cs.length>=4) holes[i++] = gf.createLinearRing(cs );
				else return null;
			}

			//return polygon
			return gf.createPolygon(shell, holes);
		} else if(geom instanceof MultiPolygon){
			//System.out.println("MultiPolygon not supported yet");
			return null;

			//get the cpx face

			//get all the lines


		} else {
			System.err.println("Error in partitionning: Unexpected geometry type (should by surfacic). " + geom.getGeometryType());
			return null;
		}
	}

}
