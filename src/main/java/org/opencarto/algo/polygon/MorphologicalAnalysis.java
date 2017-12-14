/**
 * 
 */
package org.opencarto.algo.polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.opencarto.algo.noding.NodingUtil;
import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * @author julien Gaffuri
 *
 */
public class MorphologicalAnalysis {
	private final static Logger LOGGER = Logger.getLogger(MorphologicalAnalysis.class.getName());

	private static int ID=0;

	public static Collection<Feature> runStraitAndBaysDetection(Collection<Feature> units, double resolution, double sizeDel, int quad) {

		//make quadtree of all features, for later spatial queries
		Quadtree index = new Quadtree();
		for(Feature unit : units) index.insert(unit.getGeom().getEnvelopeInternal(), unit);

		//prepare quadtree for straits
		Quadtree indexS = new Quadtree();

		double buff = 0.1*resolution;

		//detect straits for each feature
		ArrayList<Feature> straits = new ArrayList<Feature>();
		for(Feature unit : units){
			System.out.println(unit.id);
			Geometry g = unit.getGeom()
					.buffer( 0.5*resolution, quad, BufferParameters.CAP_ROUND)
					.buffer(-0.5*resolution, quad, BufferParameters.CAP_ROUND)
					.difference( unit.getGeom() )
					.buffer(-buff*0.1, quad, BufferParameters.CAP_ROUND)
					.buffer(1.1*buff, quad, BufferParameters.CAP_ROUND)
					;

			//get individual polygons
			Collection<Geometry> polys = JTSGeomUtil.getGeometries(g);
			g = null;

			//filter to keep only large ones
			HashSet<Polygon> polysFil = new HashSet<Polygon>();
			for(Geometry poly : polys){
				if(poly.getArea()<sizeDel) continue;
				polysFil.add((Polygon)poly);
			}
			polys = null;

			for(Geometry poly : polysFil) {

				//TODO factor that?
				//remove other units's parts for each patch
				for(Object o : index.query(poly.getEnvelopeInternal())){
					Feature f_ = (Feature)o;
					if(unit==f_) continue;
					Geometry g_ = f_.getGeom();
					try {
						if(!poly.getEnvelopeInternal().intersects(g_.getEnvelopeInternal())) continue;

						Geometry inter = null;
						try { inter = poly.intersection(g_); }
						catch (Exception e) { inter = poly.buffer(-buff*0.1).intersection(g_); }

						if(inter==null || inter.isEmpty()) continue;
						inter = inter.buffer(buff, quad, BufferParameters.CAP_ROUND);
						if(!(inter instanceof MultiPolygon || inter instanceof Polygon)) inter = JTSGeomUtil.keepOnlyPolygonal(inter);
						if(inter.isEmpty() || inter.getDimension()<2 || inter.getArea()==0) continue;

						try { poly = poly.difference(inter); }
						catch (Exception e) { poly = poly.buffer(buff*0.01).difference(inter.buffer(buff*0.01)); }
						if(!(poly instanceof MultiPolygon || poly instanceof Polygon)) poly = JTSGeomUtil.keepOnlyPolygonal(poly);
					} catch (Exception e) {
						System.err.println("Could not remove ground part for strait detection of "+unit.id+". "+e.getMessage());
						e.printStackTrace();
					}
				}

				//TODO factor that?
				//remove other strait's parts for each patch
				for(Object o : indexS.query(poly.getEnvelopeInternal())){
					Feature f_ = (Feature)o;
					Geometry g_ = f_.getGeom();
					try {
						if(!poly.getEnvelopeInternal().intersects(g_.getEnvelopeInternal())) continue;

						Geometry inter = null;
						try { inter = poly.intersection(g_); }
						catch (Exception e) { inter = poly.buffer(-buff*0.1).intersection(g_); }

						if(inter==null || inter.isEmpty()) continue;
						inter = inter.buffer(buff, quad, BufferParameters.CAP_ROUND);
						if(!(inter instanceof MultiPolygon || inter instanceof Polygon)) inter = JTSGeomUtil.keepOnlyPolygonal(inter);
						if(inter.isEmpty() || inter.getDimension()<2 || inter.getArea()==0) continue;

						try { poly = poly.difference(inter); }
						catch (Exception e) { poly = poly.buffer(buff*0.01).difference(inter.buffer(buff*0.01)); }
						if(!(poly instanceof MultiPolygon || poly instanceof Polygon)) poly = JTSGeomUtil.keepOnlyPolygonal(poly);
					} catch (Exception e) {
						System.err.println("Could not remove other strait part for strait detection of "+unit.id+". "+e.getMessage());
						e.printStackTrace();
					}
				}

				//get individual parts
				Collection<Geometry> polys_ = JTSGeomUtil.getGeometries(poly);
				poly = null;
				for(Geometry poly_ : polys_) {
					//keep only large parts
					if(poly_.isEmpty() || poly_.getDimension()<2 || poly_.getArea()<=sizeDel) continue;

					//save feature
					Feature strait = new Feature();
					strait.id = "S"+(ID++);
					if(! poly_.isSimple()) System.err.println("Non simple polygon for "+strait.id);
					if(! poly_.isValid()) System.err.println("Non valid polygon for "+strait.id);
					strait.setGeom((Polygon)poly_);
					strait.getProperties().put("id", strait.id);
					strait.getProperties().put("unit_id", unit.id);
					straits.add(strait);
					indexS.insert(strait.getGeom().getEnvelopeInternal(), strait);
				}
			}
		}


		System.out.println("Check no strait intersects unit which is not his");
		for(Feature strait : straits){
			Geometry sg = strait.getGeom();
			for(Object o : index.query(sg.getEnvelopeInternal())){
				Feature unit = (Feature)o;
				if(strait.getProperties().get("unit_id") == unit.id) continue;
				double area=0;
				try {
					Geometry ug = unit.getGeom();
					if(!ug.getEnvelopeInternal().intersects(sg.getEnvelopeInternal())) continue;
					Geometry inter = ug.intersection(sg);
					if(inter.isEmpty()) continue;
					area = inter.getArea();
					if(area==0) continue;
					//if(area<=0.1) continue;
					System.err.println("Strait "+strait.id+" (linked to "+strait.getProperties().get("unit_id")+") intersects unit "+unit.id+". area = "+area);
				} catch (Exception e) {
					System.err.println("Failed checking if strait "+strait.id+" (linked to "+strait.getProperties().get("unit_id")+") intersects unit "+unit.id+". area = "+area);
					//e.printStackTrace();
				}
			}
		}

		System.out.println("Check straits do not intersect each other");
		for(Feature strait1 : straits){
			Geometry sg1 = strait1.getGeom();
			for(Object o : indexS.query(sg1.getEnvelopeInternal())){
				Feature strait2 = (Feature)o;
				double area=0;
				try {
					if(strait1==strait2) continue;
					Geometry sg2 = strait2.getGeom();
					if(!sg2.getEnvelopeInternal().intersects(sg1.getEnvelopeInternal())) continue;
					Geometry inter = sg2.intersection(sg1);
					if(inter.isEmpty()) continue;
					area = inter.getArea();
					if(area==0) continue;
					//if(area<=0.1) continue;
					System.err.println(strait1.id+" intersects "+strait2.id+". area = "+area);
				} catch (Exception e) {
					System.err.println("Failed checking if "+strait1.id+" intersects "+strait2.id+". area = "+area);
					//e.printStackTrace();
				}
			}
		}

		return straits;
	}






	//Narrow parts and gaps (NPG) detection

	public static Collection<Feature> getNarrowGaps(Collection<Feature> units, double resolution, double sizeDel, int quad, int epsg) {
		ArrayList<Feature> out = new ArrayList<Feature>();
		for(Feature unit : units) {
			LOGGER.debug(unit.id);
			Collection<Polygon> ngs = getNarrowGaps(unit.getGeom(), resolution, sizeDel, quad);
			for(Polygon p : ngs) out.add(buildNPGFeature(p, "NG", unit.id, epsg));
		}
		return out;
	}

	public static Collection<Feature> getNarrowPartsAndGaps(Collection<Feature> units, double resolution, double sizeDel, int quad, int epsg) {
		ArrayList<Feature> out = new ArrayList<Feature>();
		for(Feature unit : units) {
			LOGGER.debug(unit.id);
			Object[] npg = getNarrowPartsAndGaps(unit.getGeom(), resolution, sizeDel, quad);
			for(Polygon p : (Collection<Polygon>)npg[0]) out.add(buildNPGFeature(p, "NP", unit.id, epsg));
			for(Polygon p : (Collection<Polygon>)npg[1]) out.add(buildNPGFeature(p, "NG", unit.id, epsg));
		}
		return out;
	}

	private static Feature buildNPGFeature(Polygon p, String NPGType, String unitId, int epsg){
		Feature f = new Feature(); f.setGeom(p); f.setProjCode(epsg); f.getProperties().put("NPGtype", NPGType); f.getProperties().put("unitId", unitId);
		return f;
	}

	public static Object[] getNarrowPartsAndGaps(Geometry geom, double resolution, double sizeDel, int quad) {
		return new Object[]{
				getNarrowParts(geom, resolution, sizeDel, quad),
				getNarrowGaps(geom, resolution, sizeDel, quad)
		};
	}

	public static double EPSILON = 0.00001;
	public static Collection<Polygon> getNarrowGaps(Geometry geom, double resolution, double sizeDel, int quad) {
		Geometry geom_ = geom
				.buffer( 0.5*resolution, quad, BufferParameters.CAP_ROUND)
				.buffer(-0.5*(1+EPSILON)*resolution, quad, BufferParameters.CAP_ROUND);
		geom_ = geom_.difference(geom)
				.buffer(EPSILON*resolution, quad, BufferParameters.CAP_ROUND);
		//catch (Exception e) { geom_ = geom_.difference( geom.buffer(resolution*0.0000001) ); }
		if(geom_==null || geom_.isEmpty()) return new ArrayList<Polygon>();
		return JTSGeomUtil.getPolygonGeometries(geom_, sizeDel);
	}

	public static Collection<Polygon> getNarrowParts(Geometry geom, double resolution, double sizeDel, int quad) {
		Geometry geom_ = geom
				.buffer(-0.5*resolution, quad, BufferParameters.CAP_ROUND)
				.buffer( 0.5*(1+EPSILON)*resolution, quad, BufferParameters.CAP_ROUND);
		geom_ = geom.difference(geom_)
				.buffer(EPSILON*resolution, quad, BufferParameters.CAP_ROUND);
		//catch (Exception e) { geom_ = geom.difference(geom_.buffer(EPSILON)); }
		if(geom_==null || geom_.isEmpty()) return new ArrayList<Polygon>();
		return JTSGeomUtil.getPolygonGeometries(geom_, sizeDel);
	}

	public static MultiPolygon    fillNarrowGaps(Geometry geom, double resolution, double sizeDel, int quad) { return _n( 1, geom, resolution, sizeDel, quad); }
	public static MultiPolygon removeNarrowParts(Geometry geom, double resolution, double sizeDel, int quad) { return _n(-1, geom, resolution, sizeDel, quad); }
	private static MultiPolygon _n(int multi, Geometry geom, double resolution, double sizeDel, int quad) {
		Geometry geom_ = geom
				.buffer( multi*0.5*resolution, quad, BufferParameters.CAP_ROUND)
				.buffer(-multi*0.5*resolution, quad, BufferParameters.CAP_ROUND);
		return (MultiPolygon) JTSGeomUtil.toMulti(geom_);
	}



	public static void removeNarrowGapsTesselation(Collection<Feature> units, double resolution, double sizeDel, int quad, double nodingResolution) {
		boolean b;

		//build spatial index of all features
		Quadtree index = new Quadtree();
		for(Feature unit : units) index.insert(unit.getGeom().getEnvelopeInternal(), unit);

		int nb=0;
		//handle units one by one
		for(Feature unit : units) {
			LOGGER.info(unit.id + " - " + 100.0*(nb++)/units.size());

			//get narrow gaps
			Collection<Polygon> ngs = getNarrowGaps(unit.getGeom(), resolution, sizeDel, quad);

			for(Polygon ng : ngs) {
				ng = (Polygon) ng.buffer(resolution*0.001, quad);
				Geometry newUnitGeom = null;
				try {
					newUnitGeom = unit.getGeom().union(ng);
				} catch (Exception e1) {
					LOGGER.warn("Could not make union of unit "+unit.id+" with gap around " + ng.getCentroid().getCoordinate() + " Exception: "+e1.getClass().getName());
					continue;
				}

				//get units intersecting and correct their geometries
				Collection<Feature> uis = index.query( ng.getEnvelopeInternal() );
				//uis = getTrue(uis, ng.getEnvelopeInternal());
				for(Feature ui : uis) {
					if(ui == unit) continue;
					if(!ui.getGeom().getEnvelopeInternal().intersects(ng.getEnvelopeInternal())) continue;

					Geometry geom_ = null;
					try { geom_ = ui.getGeom().difference(ng); } catch (Exception e) {}
					if(geom_==null || geom_.isEmpty()) {
						LOGGER.warn("Unit "+ui.id+" disappeared when removing gaps of unit "+unit.id+" around "+ng.getCentroid().getCoordinate());
						newUnitGeom = newUnitGeom.difference(ui.getGeom());
						continue;
					} else {
						//set new geometry - update index
						b = index.remove(ui.getGeom().getEnvelopeInternal(), ui);
						if(!b) LOGGER.warn("Could not update index for "+ui.id+" while removing narrow gap of "+unit.id+" around "+ng.getCentroid().getCoordinate());
						ui.setGeom(JTSGeomUtil.toMulti(geom_));
						index.insert(ui.getGeom().getEnvelopeInternal(), ui);
					}
				}

				//set new geometry - update index
				b = index.remove(unit.getGeom().getEnvelopeInternal(), unit);
				if(!b) LOGGER.warn("Could not update index for "+unit.id+" while removing narrow gaps around "+unit.getGeom().getCentroid().getCoordinate());
				unit.setGeom(JTSGeomUtil.toMulti(newUnitGeom));
				index.insert(unit.getGeom().getEnvelopeInternal(), unit);
			}

		}

		if(nodingResolution > 0) {
			LOGGER.info("Ensure noding");
			NodingUtil.fixNoding(units, nodingResolution);
		}
	}

	/*
	public static Collection<Feature> getTrue(Collection<Feature> in, Envelope env){
		Collection<Feature> out = new HashSet<Feature>();
		for(Feature f : in)
			if(f.getGeom().getEnvelopeInternal().intersects(env))
				out.add(f);
		return out;
	}

	//ensure noding of feature geometries (multipolygons)
	private static void ensureNoding(Collection<Feature> uis) {

		//build graph
		Collection<MultiPolygon> unitGeoms = new HashSet<MultiPolygon>();
		for(Feature ui : uis) unitGeoms.add((MultiPolygon)ui.getGeom());
		Graph g = GraphBuilder.build(unitGeoms);
		unitGeoms = null;

		//retrieve faces and link them to features
		HashMap<Face,Feature> map = new HashMap<Face,Feature>();
		for(Face f : g.getFaces()) {
			Geometry patch = f.getGeometry();
			Feature fBest = null; double maxArea=-1;
			for(Feature feat : uis) {
				if(!patch.getEnvelopeInternal().intersects(feat.getGeom().getEnvelopeInternal())) continue;
				if(!patch.intersects(feat.getGeom())) continue;
				Geometry inter = patch.intersection(feat.getGeom());
				if(inter == null || inter.isEmpty()) continue;
				double area = inter.getArea();
				if(area == 0) continue;
				if(area < maxArea) continue;
				fBest = feat; maxArea = area;
			}
			if(fBest == null) {
				//LOGGER.error("Could not find unit for face when noding polygons. Postion: "+patch.getCentroid().getCoordinate());
				continue;
			}
			map.put(f, fBest);
		}

		//rebuild unit geometries
		for(Feature ui : uis) ui.setGeom(ui.getGeom().getFactory().buildGeometry(new HashSet<Geometry>()));
		for(Entry<Face,Feature> e: map.entrySet()) {
			Geometry patch = e.getKey().getGeometry();
			Feature f = e.getValue();
			f.setGeom((MultiPolygon)JTSGeomUtil.toMulti(f.getGeom().union(patch)));
		}
	}
	 */



	/*public static void removeNarrowGapsTesselation(Collection<Feature> units, double resolution, int quad) {
		boolean b;

		//make quadtree of all features, for later spatial queries
		Quadtree index = new Quadtree();
		for(Feature unit : units) index.insert(unit.getGeom().getEnvelopeInternal(), unit);

		int nb=0;
		//handle units one by one
		for(Feature unit : units) {
			LOGGER.info(unit.id + " - " + 100.0*(nb++)/units.size());
			//Util.printProgress(nb++, units.size());

			//compute new geometry without narrow gaps
			Geometry geomNG = unit.getGeom()
					.buffer( 0.5*resolution, quad, BufferParameters.CAP_ROUND)
					.buffer(-0.5*resolution, quad, BufferParameters.CAP_ROUND);
			if(geomNG==null || geomNG.isEmpty()) {
				LOGGER.warn("Could not remove narrow gaps for unit "+unit.id);
				continue;
			};

			//get units intersecting the new geometry and try to correct their geometries
			List<Feature> uis = index.query(geomNG.getEnvelopeInternal());
			for(Feature ui : uis) {
				if(ui == unit) continue;

				Geometry geom_ = null;
				try {
					geom_ = ui.getGeom().difference(geomNG);
				} catch (Exception e) {
					geom_ = null;
				}

				if(geom_==null || geom_.isEmpty()) {
					LOGGER.info("Unit "+ui.id+" disappeared when removing gaps of unit "+unit.id);
					geomNG = geomNG.difference(ui.getGeom());
					continue;
				} else {
					//set new geometry - update index
					b = index.remove(ui.getGeom().getEnvelopeInternal(), ui);
					if(!b) LOGGER.warn("Could not update index for "+ui.id+" while removing narrow gaps of "+unit.id);
					ui.setGeom(JTSGeomUtil.toMulti(geom_)); geom_ = null;
					index.insert(ui.getGeom().getEnvelopeInternal(), ui);

				}

			}

			//set new geometry - update index
			b = index.remove(unit.getGeom().getEnvelopeInternal(), unit);
			if(!b) LOGGER.warn("Could not update index for "+unit.id+" while removing narrow gaps.");
			unit.setGeom(JTSGeomUtil.toMulti(geomNG)); geomNG = null;
			index.insert(unit.getGeom().getEnvelopeInternal(), unit);
		}
	}*/

}
