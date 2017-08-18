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

	public static Collection<Feature> runStraitAndBaysDetection(Collection<Feature> fs, double resolution, double sizeDel, int quad) {

		//make quadtree of all features, for later spatial queries
		Quadtree index = new Quadtree();
		for(Feature f : fs) index.insert(f.getGeom().getEnvelopeInternal(), f);

		//detect straits for each feature
		ArrayList<Feature> fsOut = new ArrayList<Feature>();
		for(Feature f : fs){
			//System.out.println(f.id);
			Geometry g;
			g = BufferOp.bufferOp(f.getGeom(),  0.5*resolution, quad, BufferParameters.CAP_ROUND);
			g = BufferOp.bufferOp(g, -0.5*resolution, quad, BufferParameters.CAP_ROUND);
			//g = JTSGeomUtil.keepOnlyPolygonal(g);
			//g = JTSGeomUtil.keepOnlyPolygonal( g.symDifference(f.getGeom()) );
			g = g.symDifference(f.getGeom());

			//get individual polygons
			Collection<Geometry> polys = JTSGeomUtil.getGeometries(g);
			g = null;

			//filter to keep only large polygons
			HashSet<Geometry> polysFil = new HashSet<Geometry>();
			for(Geometry poly : polys)
				if(poly.getArea()>=sizeDel) polysFil.add((MultiPolygon) JTSGeomUtil.toMulti((Polygon)poly));
			polys = null;

			for(Geometry poly : polysFil) {

				//remove other units's parts for each patch
				for(Object o : index.query(poly.getEnvelopeInternal())){
					try {
						Feature f_ = (Feature)o;
						if(f==f_) continue;
						Geometry g_ = f_.getGeom();
						if(!g_.getEnvelopeInternal().intersects(poly.getEnvelopeInternal())) continue;
						if(!g_.intersects(poly)) continue;

						Geometry inter = poly.intersection(g_);
						if(inter.isEmpty() || inter.getDimension()<2 || inter.getArea()==0) continue;

						poly = poly.symDifference(inter);
					} catch (Exception e) {
						System.err.println("Could not remove ground part for strait detection of "+f.id+". "+e.getMessage());
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
					Feature fOut = new Feature();
					fOut.setGeom((Polygon)poly_);
					fOut.getProperties().put("unit_id", f.id);
					fsOut.add(fOut);
				}
			}
		}

		return fsOut;
	}

}
