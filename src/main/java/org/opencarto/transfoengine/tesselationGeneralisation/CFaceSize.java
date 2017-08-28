/**
 * 
 */
package org.opencarto.transfoengine.tesselationGeneralisation;

import java.util.ArrayList;
import java.util.List;

import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.transfoengine.Agent;
import org.opencarto.transfoengine.Constraint;
import org.opencarto.transfoengine.Transformation;

/**
 * @author julien Gaffuri
 *
 */
public class CFaceSize extends Constraint {

	private double minSizeDel, minSize;

	public CFaceSize(Agent agent, double minSizeDel, double minSize) {
		super(agent);
		this.minSizeDel=minSizeDel;
		this.minSize=minSize;
	}



	double initialValue, currentValue, goalValue;

	@Override
	public void computeInitialValue() {
		computeCurrentValue();
		initialValue = currentValue;
	}

	@Override
	public void computeCurrentValue() {
		Face d = (Face)(getAgent().getObject());
		currentValue = d.getGeometry()==null? 0 : d.getGeometry().getArea();
	}

	@Override
	public void computeGoalValue() {
		goalValue = initialValue>minSize ? initialValue : initialValue<minSizeDel? 0 : minSize;
	}




	@Override
	public void computeSatisfaction() {
		if(getAgent().isDeleted())
			if(goalValue == 0) satisfaction=10; else satisfaction=0;
		else
			if(goalValue == 0) satisfaction=0;
			else satisfaction = 10 - 10*Math.abs(goalValue-currentValue)/goalValue;
	}

	@Override
	public List<Transformation<?>> getTransformations() {
		ArrayList<Transformation<?>> out = new ArrayList<Transformation<?>>();

		AFace aFace = (AFace)getAgent();
		Face f = aFace.getObject();

		//deletion case
		if(goalValue == 0){

			//islands case
			if(f.isIsland()){
				if( ! aFace.isTheLastUnitPatchToRemove() ) {
					//propose deletion
					out.add(new TFaceIslandDeletion(aFace));
				}
			}


			//enclave case
			//TODO should be covered by general case
			else if(f.isEnclave()){
				if( ! aFace.isTheLastUnitPatchToRemove() ) {
					//propose enclave deletion, that is aggregation with the other face around
					Edge edge = aFace.getObject().getEdges().iterator().next();
					Face otherFace = edge.f1==aFace.getObject()? edge.f2 : edge.f1;
					out.add(new TFaceAggregation(aFace, otherFace));
				}
			}


			//other case
			else {
				if( ! aFace.isTheLastUnitPatchToRemove() ) {
					//check if good aggregation candidate exists. If yes, aggregate else collapse

					//best edge for aggregation is the one having the maximum length and having another face. Maybe the other face's size could also be considered?
					Face maxLengthFace=null; double maxLength=-1;
					for(Face f2:f.getTouchingFaces()){
						double length = f.getLength(f2);
						if(length<maxLength) continue;
						maxLengthFace = f2; maxLength = length;
					}

					if(maxLengthFace == null) {
						System.err.println("Could not find good candidate face for aggregation of face "+f.getId()+". Number of edges of face: "+f.getEdges().size());
						if(aFace.aUnit != null) System.err.println("Unit Id: "+aFace.aUnit.getId());
						for(Edge e:f.getEdges()) System.err.println(e.getId()+"   "+(e.f1!=null?e.f1.getId():"")+"   "+(e.f2!=null?e.f2.getId():""));
						//aFace.getAtesselation().exportFacesAsSHP("/home/juju/Bureau/out/", "faces_input.shp", 3035);
						//aFace.getAtesselation().exportEdgesAsSHP("/home/juju/Bureau/out/", "edges_input.shp", 3035);
						//aFace.getAtesselation().exportNodesAsSHP("/home/juju/Bureau/out/", "nodes_input.shp", 3035);
					}

					//propose aggregation
					out.add(new TFaceAggregation(aFace, maxLengthFace));

					//TODO improve candidate selection method and propose also face collapse if several equivalent candidates are found.
				}
			}

		}
		return out;
	}

}
