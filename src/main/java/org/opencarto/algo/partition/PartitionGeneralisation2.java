package org.opencarto.algo.partition;

import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.algo.base.DouglasPeuckerRamerFilter;
import org.opencarto.algo.base.VertexRemoval;
import org.opencarto.io.SHPUtil;
import org.opencarto.util.JTSGeomUtil;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * @author julien Gaffuri
 *
 */
public class PartitionGeneralisation2 {

	public static void generalisePartition(ArrayList<SimpleFeature> fs, String geomAtt, double dp) {

		//go through features
		SimpleFeature fi,fj;
		MultiPolygon geomi,geomj;
		for(int i=0; i<fs.size(); i++){
			fi = fs.get(i);
			geomi = (MultiPolygon)JTSGeomUtil.toMulti((Geometry)fi.getAttribute(geomAtt));
			if(geomi == null || geomi.isEmpty()) continue;
			for(int j=i+1; j<fs.size(); j++){
				fj = fs.get(j);
				geomj = (MultiPolygon)JTSGeomUtil.toMulti((Geometry)fj.getAttribute(geomAtt));
				if(geomj == null || geomj.isEmpty()) continue;

				System.out.println("   ("+(i+1)+","+(j+1)+") "+fs.size());

				//if(!geomi.getEnvelopeInternal().intersects(geomj.getEnvelopeInternal())) continue;
				System.out.print("Check intersection... ");
				if(!geomi.intersects(geomj)) {
					System.out.println(" No");
					continue;
				}
				System.out.println(" Yes");

				System.out.println("Extract linear geometries...");
				MultiLineString geomiL = JTSGeomUtil.keepOnlyLinear(geomi);
				MultiLineString geomjL = JTSGeomUtil.keepOnlyLinear(geomj);

				System.out.print("Compute intersection...");
				Geometry inter;
				try {
					inter = geomiL.intersection(geomjL);
				} catch (Exception e) {
					System.err.println(" Failed!");
					continue;
				}
				System.out.println(" Done!");
				geomiL=null; geomjL=null;

				System.out.println("Extract intersection linear geometries...");
				MultiLineString interMLS = JTSGeomUtil.keepOnlyLinear(inter);
				inter=null;

				if(interMLS==null) continue;
				if(interMLS.isEmpty()) continue;
				if(interMLS.getCoordinates().length<=2) continue;
				if(interMLS.getLength()==0) continue;

				System.out.println("Clean intersection");
				interMLS = (MultiLineString)JTSGeomUtil.toMulti(JTSGeomUtil.clean(interMLS));

				//fi and fj touch: Simplify their shared outline

				System.out.print("Get all parts");
				Collection<Geometry> inters = JTSGeomUtil.getGeometries(interMLS);
				interMLS=null;
				System.out.println(" -> "+inters.size());

				//apply filter
				for(Geometry interLine_ : inters){
					LineString interLine = (LineString)interLine_;
					//get points to remove
					System.out.print("Compute DP filter");
					ArrayList<Coordinate> cRem = DouglasPeuckerRamerFilter.getCoordinatesToRemove(interLine, dp);
					if(cRem.size()==0) {
						System.out.println(" -> No filtering needed.");
						continue;
					}

					System.out.println(" -> "+cRem.size()+" point(s) to remove ("+(int)(100*cRem.size()/interLine.getCoordinates().length)+"%)");

					//remove points
					System.out.print("Remove vertices. i:"+geomi.getCoordinates().length+", j:"+geomj.getCoordinates().length);
					MultiPolygon[] gOut = VertexRemoval.remove(geomi, geomj, cRem);
					geomi = gOut[0];
					geomj = gOut[1];
					System.out.println(" -> i:"+geomi.getCoordinates().length+", j:"+geomj.getCoordinates().length);
				}
				System.out.println("Update fj");
				fj.setAttribute(geomAtt, geomj);
			}
			System.out.println("Update fi");
			fi.setAttribute(geomAtt, geomi);
		}

		System.out.println("Clean polygons");
		for(SimpleFeature f:fs)
			f.setAttribute(geomAtt, JTSGeomUtil.toMulti(((Geometry)f.getAttribute(geomAtt)).buffer(0)));

		System.out.print("Remove empty: "+fs.size());
		SHPUtil.removeNullOrEmpty(fs, geomAtt);
		System.out.println(" -> "+fs.size());
	}
}
