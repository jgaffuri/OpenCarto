/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * @author julien Gaffuri
 *
 */
public class EdgeToEdgeIntersection extends Constraint {
	SpatialIndex edgeSpatialIndex;

	public EdgeToEdgeIntersection(Agent agent, SpatialIndex edgeSpatialIndex) {
		super(agent);
		this.edgeSpatialIndex = edgeSpatialIndex;
	}

	boolean intersects = false;

	@Override
	public void computeCurrentValue() {
		Edge e = (Edge)getAgent().getObject();
		LineString g = e.getGeometry();

		//retrieve edges from spatial index
		List<Edge> edges = edgeSpatialIndex.query(g.getEnvelopeInternal());
		for(Edge e_ : edges){
			LineString g_ = e_.getGeometry();
			if(!g_.getEnvelopeInternal().intersects(g.getEnvelopeInternal())) continue;

			//analyse intersection
			Geometry inter = g.intersection(g_);
			if(inter.isEmpty()) continue;
			if(inter.getLength()>0){
				intersects = true;
				return;
			}
			for(Coordinate c : inter.getCoordinates()){
				if( c.distance(e.getN1().c)==0 || c.distance(e.getN2().c)==0 ) continue;
				intersects = true;
				return;
			}
		}
		intersects = false;
	}

	@Override
	public void computeGoalValue() {}

	@Override
	public void computeSatisfaction() {
		satisfaction = intersects?0:10;
	}

	@Override
	public boolean isHard() { return true; }

}
