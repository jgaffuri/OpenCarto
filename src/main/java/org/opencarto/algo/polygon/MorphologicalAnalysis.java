/**
 * 
 */
package org.opencarto.algo.polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;
import org.opencarto.util.JTSGeomUtil;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * @author julien Gaffuri
 *
 */
public class MorphologicalAnalysis {

	public static Collection<Feature> runStraitAndBaysDetection(Collection<Feature> units, double resolution, double sizeDel, int quad) {

		//make quadtree of all features, for later spatial queries
		Quadtree index = new Quadtree();
		for(Feature unit : units) index.insert(unit.getGeom().getEnvelopeInternal(), unit);

		//detect straits for each feature
		ArrayList<Feature> straits = new ArrayList<Feature>();
		for(Feature unit : units){
			//System.out.println(f.id);
			Geometry g;
			g = BufferOp.bufferOp(unit.getGeom(),  0.5*resolution, quad, BufferParameters.CAP_ROUND);
			g = BufferOp.bufferOp(g, -0.5*resolution, quad, BufferParameters.CAP_ROUND);
			//g = JTSGeomUtil.keepOnlyPolygonal(g);
			//g = JTSGeomUtil.keepOnlyPolygonal( g.symDifference(f.getGeom()) );
			g = g.symDifference(unit.getGeom());

			//get individual polygons
			Collection<Geometry> polys = JTSGeomUtil.getGeometries(g);
			g = null;

			//filter to keep only large ones
			HashSet<Geometry> polysFil = new HashSet<Geometry>();
			for(Geometry poly : polys)
				if(poly.getArea()>=sizeDel) polysFil.add((MultiPolygon) JTSGeomUtil.toMulti((Polygon)poly));
			polys = null;

			for(Geometry poly : polysFil) {

				//remove other units's parts for each patch
				for(Object o : index.query(poly.getEnvelopeInternal())){
					Feature f_ = (Feature)o;
					if(unit==f_) continue;
					Geometry g_ = f_.getGeom();
					try {
						if(!poly.getEnvelopeInternal().intersects(g_.getEnvelopeInternal())) continue;

						//if(!(poly instanceof MultiPolygon)) poly = JTSGeomUtil.keepOnlyPolygonal(poly);
						if(!(g_ instanceof MultiPolygon)) g_ = JTSGeomUtil.keepOnlyPolygonal(g_);
						//if(!poly.intersects(g_)) continue; //maybe this is not necessary...

						Geometry inter = poly.intersection(g_);
						if(inter.isEmpty()) continue;
						if(!(inter instanceof MultiPolygon)) inter = JTSGeomUtil.keepOnlyPolygonal(inter);
						if(inter.isEmpty() || inter.getDimension()<2 || inter.getArea()==0) continue;
						poly = poly.symDifference(inter);

						//poly = poly.symDifference(g_);
					} catch (Exception e) {
						System.err.println("Could not remove ground part for strait detection of "+unit.id+". "+e.getMessage());
						e.printStackTrace();
					}
				}

				//get individual parts
				Collection<Geometry> polys_ = JTSGeomUtil.getGeometries(poly);
				poly=null;
				for(Geometry poly_ : polys_) {
					//keep only large parts
					if(poly_.isEmpty() || poly_.getDimension()<2 || poly_.getArea()<=sizeDel) continue;

					//save feature
					Feature strait = new Feature();
					strait.setGeom((Polygon)poly_);
					strait.getProperties().put("unit_id", unit.id);
					straits.add(strait);
				}
			}
		}


		System.out.println("Check no strait intersects unit");
		//TODO
		System.out.println("Check straits do not intersect");
		//TODO



		return straits;
	}

}
