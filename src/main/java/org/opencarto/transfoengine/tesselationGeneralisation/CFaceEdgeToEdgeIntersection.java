/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Constraint;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * Ensures that none of the edges of the face intersects other edges.
 * 
 * @author julien Gaffuri
 *
 */
public class CFaceEdgeToEdgeIntersection extends Constraint<AFace> {
	SpatialIndex edgeSpatialIndex;

	public CFaceEdgeToEdgeIntersection(AFace agent, SpatialIndex edgeSpatialIndex) {
		super(agent);
		this.edgeSpatialIndex = edgeSpatialIndex;
	}

	boolean intersectsOthers = false;

	@Override
	public void computeCurrentValue() {
		Face f = getAgent().getObject();
		for(Edge e : f.getEdges()){
			LineString g = e.getGeometry();

			//retrieve edges from spatial index
			List<Edge> edges = edgeSpatialIndex.query(g.getEnvelopeInternal());
			for(Edge e_ : edges){
				if(e==e_) continue;

				LineString g_ = e_.getGeometry();
				if(!g_.getEnvelopeInternal().intersects(g.getEnvelopeInternal())) continue;

				//analyse intersection
				//TODO improve speed by using right geometrical predicate. crosses?
				Geometry inter = g.intersection(g_);
				if(inter.isEmpty()) continue;
				if(inter.getLength()>0){
					//System.out.println("  length!"+e.getId()+" "+e_.getId());
					intersectsOthers = true;
					return;
				}
				for(Coordinate c : inter.getCoordinates()){
					if( c.distance(e.getN1().getC())==0 || c.distance(e.getN2().getC())==0 ) continue;
					//System.out.println("  coord!"+e.getId()+" "+e_.getId());
					intersectsOthers = true;
					return;
				}
			}
		}
		intersectsOthers = false;
	}

	@Override
	public void computeSatisfaction() {
		satisfaction = intersectsOthers? 0 : 10;
	}

	@Override
	public boolean isHard() { return true; }

}
