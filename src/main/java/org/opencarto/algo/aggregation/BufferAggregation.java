/**
 * 
 */
package org.opencarto.algo.aggregation;

import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.algo.base.Closure;
import org.opencarto.algo.base.DouglasPeuckerRamerFilter;
import org.opencarto.algo.base.Union;
import org.opencarto.algo.polygon.HolesDeletion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * 
 * Aggregation of a set of geometry buffers
 * 
 * @author julien Gaffuri
 *
 */
public class BufferAggregation{
	private double bufferDist;
	private double erosionDist;
	private int qSegs;
	private double dPThreshold;
	private boolean withHoleDeletion;

	public BufferAggregation(double bufferDist, double erosionDist, int qSegs, double dPThreshold, boolean withHoleDeletion) {
		super();
		this.bufferDist = bufferDist;
		this.erosionDist = erosionDist;
		this.qSegs = qSegs;
		this.dPThreshold = dPThreshold;
		this.withHoleDeletion = withHoleDeletion;
	}

	public Geometry aggregateGeometries(Collection<Geometry> geoms){
		ArrayList<Geometry> buffs = new ArrayList<Geometry>();
		for(Geometry geom : geoms)
			buffs.add( geom.buffer(bufferDist, qSegs) );

		Geometry out = Union.getPolygonUnion(buffs);
		buffs.clear();

		if(dPThreshold>0) out = DouglasPeuckerRamerFilter.get(out, dPThreshold);
		out = Closure.get(out, erosionDist, qSegs, BufferParameters.CAP_ROUND );
		if(dPThreshold>0) out = DouglasPeuckerRamerFilter.get(out, dPThreshold);
		out = out.buffer(0);
		if(withHoleDeletion) {
			if (out instanceof Polygon) out = HolesDeletion.get((Polygon)out);
			else if (out instanceof MultiPolygon) out = HolesDeletion.get((MultiPolygon)out);
			else return null;
		}
		return out;
	}

	/*
	private static Feature aggregate(Feature f1, Feature f2, int z, double dist) {
		Feature fag = new Feature();

		Geometry g1 = f1.getGeom(z);
		Geometry g2 = f2.getGeom(z);

		if(g1.getArea()==0) g1=g1.buffer(dist);
		if(g2.getArea()==0) g2=g1.buffer(dist);
		Geometry g = (g1.buffer(dist).union(g2.buffer(dist))).buffer(-dist);
		g = DouglasPeuckerRamerFilter.get(g, dist);

		fag.setGeom(g);
		fag.setGeom(g,z);

		//merge component's lists
		fag.components = new ArrayList<Feature>();
		if(f1.components==null) fag.components.add(f1); else fag.components.addAll(f1.components);
		if(f2.components==null) fag.components.add(f2); else fag.components.addAll(f2.components);

		fag.desc = fag.components.size() + " objects.";

		return fag;
	}*/
}
