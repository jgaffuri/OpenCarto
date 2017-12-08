/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * 
 * Check a unit is correctly noded to its touching ones.
 * 
 * @author julien
 *
 */
public class CUnitNoding  extends Constraint<AUnit> {
	private final static Logger LOGGER = Logger.getLogger(CUnitNoding.class.getName());

	private SpatialIndex index;
	//private TopologyException nodingException = null;
	private Collection<NodingIssue> nis = new HashSet<NodingIssue>();

	public CUnitNoding(AUnit agent, SpatialIndex index) {
		super(agent);
		this.index = index;
	}

	@Override
	public void computeCurrentValue() {
		LOGGER.info("CUnitNoding "+getAgent().getObject().id);

		Geometry geom = getAgent().getObject().getGeom();
		for(Feature au : (List<Feature>) index.query(geom.getEnvelopeInternal())) {
			Collection<NodingIssue> nis_ = analyseNoding(geom, au.getGeom());
			nis.addAll(nis_);
		}

		/*
		ArrayList<Geometry> lineCol = new ArrayList<Geometry>();
		Geometry geom = getAgent().getObject().getGeom();
		for(Feature au : (List<Feature>) index.query(geom.getEnvelopeInternal()))
			lineCol.add(au.getGeom().getBoundary());

		Geometry union = new GeometryFactory().buildGeometry(lineCol);
		try {
			union = union.union();
		} catch (TopologyException e) {
			nodingException = e;
		}*/
	}

	@Override
	public void computeSatisfaction() {
		//TODO better?
		if(nis == null || nis.size()==0) satisfaction = 10;
		else satisfaction = 0;
	}

	@Override
	public List<Transformation<AUnit>> getTransformations() {
		//TODO make noding?
		return new ArrayList<Transformation<AUnit>>();
	}

	public String getMessage(){
		StringBuffer sb = new StringBuffer(super.getMessage());
		for(NodingIssue ni : nis)
			sb.append(",").append(ni.c.toString().replace(",", ";")); //.append(",").append(ni.distance);
		return sb.toString();
	}



	private static Collection<NodingIssue> analyseNoding(Geometry g1, Geometry g2) {
		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		out.addAll(a_(g1,g2)); out.addAll(a_(g2,g1));
		return out;
	}

	private static Collection<NodingIssue> a_(Geometry g1, Geometry g2) {
		//check if points of g1 are noded to points of g2.
		GeometryFactory gf = new GeometryFactory();

		Collection<NodingIssue> out = new HashSet<NodingIssue>();
		MultiPoint g2_pt = gf.createMultiPoint(g2.getCoordinates());
		for(Coordinate c : g1.getCoordinates()) {
			Point pt = gf.createPoint(c);
			//correct noded case
			if( pt.distance(g2_pt) == 0 ) continue;
			//correct not noded case
			if( pt.distance(g2) > 0 ) continue;
			out.add( new NodingIssue(c) );
		}

		return out;
	}

	public static class NodingIssue{
		public Coordinate c;
		//public double distance;
		public NodingIssue(Coordinate c) { this.c=c; }
	}

}
