/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.transfoengine.Transformation;
import org.opencarto.util.Util;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * @author julien Gaffuri
 *
 */
public class TEdgeRamerDouglasPeuckerSimplifier extends Transformation<AEdge> {

	private double resolution;
	private boolean preserveTopology = false;

	public TEdgeRamerDouglasPeuckerSimplifier(AEdge agent, double resolution, boolean preserveTopology) {
		super(agent);
		this.resolution = resolution;
		this.preserveTopology = preserveTopology;
	}

	@Override
	public void apply() {
		Edge e = (Edge) agent.getObject();
		LineString lsIni = e.getGeometry(), lsFin;

		if(preserveTopology){
			TopologyPreservingSimplifier tr = new TopologyPreservingSimplifier(lsIni);
			tr.setDistanceTolerance(resolution);
			lsFin = (LineString) tr.getResultGeometry();
		} else {
			//LineString lsFin = (LineString) DouglasPeuckerSimplifier.simplify(lsIni, resolution);
			DouglasPeuckerSimplifier rdps = new DouglasPeuckerSimplifier(lsIni);
			rdps.setDistanceTolerance(resolution);
			rdps.setEnsureValid(true);
			lsFin = (LineString) rdps.getResultGeometry();
		}

		e.setGeom(lsFin);
	}





	@Override
	public boolean isCancelable() { return true; }

	private LineString geomStore= null;

	@Override
	public void storeState() {
		geomStore = ((Edge)agent.getObject()).getGeometry();
	}

	@Override
	public void cancel() {
		((Edge)agent.getObject()).setGeom(geomStore);
	}


	public String toString(){
		return getClass().getSimpleName() + "(res="+Util.round(resolution, 3)+";topo="+preserveTopology+")";
	}
}
