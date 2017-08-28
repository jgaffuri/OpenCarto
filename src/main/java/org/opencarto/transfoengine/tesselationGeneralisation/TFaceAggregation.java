package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class TFaceAggregation extends Transformation<AFace> {
	Face targetFace;

	public TFaceAggregation(AFace agent, Face targetFace) {
		super(agent);
		this.targetFace = targetFace;
	}



	@Override
	public void apply() {
		Face delFace = agent.getObject();
		Graph g = delFace.getGraph();

		if(delFace.isEnclave()){
			Collection<Edge> es = delFace.getEdges();
			Collection<Node> ns = delFace.getNodes();

			//remove face (making hole)
			g.remove(delFace);

			//remove hole in target face
			//remove edges
			targetFace.getEdges().removeAll(es);
			for(Edge e:es){ e.f1=null; e.f2=null; g.remove(e); }

			//remove all nodes
			for(Node n:ns) g.remove(n);

			//delete corresponding agents
			agent.setDeleted(true);
			for(Edge e:es) agent.getAtesselation().getAEdge(e).setDeleted(true);

		} else {

			//get edges to delete (the ones in common)
			Set<Edge> delEdges = delFace.getEdges(targetFace);
			if(delEdges.size()==0){
				System.err.println("Could not aggregate face "+delFace.getId()+" with face "+targetFace.getId()+": No edge in common.");
				return;
			}

			//store nodes concerned
			Set<Node> nodes = new HashSet<Node>();
			for(Edge delEdge : delEdges) { nodes.add(delEdge.getN1()); nodes.add(delEdge.getN2()); }

			//remove edges between both faces + corresponding edge agents
			for(Edge delEdge : delEdges){
				delEdge.f1=null; delEdge.f2=null;
				g.remove(delEdge);
				AEdge ea = agent.getAtesselation().getAEdge(delEdge);
				if(ea==null) System.err.println("Could not find edge agent for edge "+delEdge.getId());
				else ea.setDeleted(true);
			}
			targetFace.getEdges().removeAll(delEdges);
			delFace.getEdges().removeAll(delEdges);

			//aggregate faces
			g.remove(delFace);
			for(Edge e : delFace.getEdges()) if(e.f1==delFace) e.f1=targetFace; else e.f2=targetFace;
			targetFace.getEdges().addAll(delFace.getEdges());
			delFace.getEdges().clear();

			//delete agent face
			agent.setDeleted(true);

			//TODO
			//remove single nodes
			//ensure nodes are reduced, which means they do not have a degree 2
			//Edge e1 = delEdge.getN1().ensureReduction();
			//Edge e2 = delEdge.getN2().ensureReduction();

		}

	}



	@Override
	public boolean isCancelable() { return false; }

	@Override
	public void storeState() {}

	@Override
	public void cancel() {
		System.err.println("cancel() not implemented for "+this.getClass().getSimpleName());
	}

}
