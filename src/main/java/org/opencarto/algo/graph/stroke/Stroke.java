/**
 * 
 */
package org.opencarto.algo.graph.stroke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opencarto.algo.base.Union;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class Stroke extends Feature {
	private List<Feature> sections = new ArrayList<>();
	public List<Feature> getSections() { return sections; }

	public Stroke(Collection<Edge> edges) {
		//set list of features
		for(Edge e : edges) sections.add( (Feature)e.obj );
		//build and set geometry
		Collection<Geometry> gs = new ArrayList<Geometry>();
		for(Edge e : edges) gs.add(e.getGeometry());
		this.setGeom( Union.getUnionAsLineString(gs) );
		//set salience
		set("length",getGeom().getLength());
	}
}
