/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.GraphElement;
import org.opencarto.datamodel.graph.Node;

/**
 * 
 * Transform graph elements in features.
 * Convenient to export them and show them in a software.
 * 
 * @author julien Gaffuri
 *
 */
public class GraphToFeature {
	private final static Logger LOGGER = Logger.getLogger(GraphToFeature.class.getName());

	//node
	public static Feature asFeature(Node n){
		Feature f = new Feature();
		f.setGeom(n.getGeometry());
		f.id=n.getId();
		f.set("id", n.getId());
		f.set("value", n.value);
		f.set("edg_in_nb", n.getInEdges().size());
		f.set("edg_out_nb", n.getOutEdges().size());
		String txt=null;
		for(Edge e:n.getInEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.set("edges_in", txt);
		txt=null;
		for(Edge e:n.getOutEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.set("edges_out", txt);
		Collection<Face> faces = n.getFaces();
		f.set("face_nb", faces .size());
		txt=null;
		for(Face d:faces) txt=(txt==null?"":txt+";")+d.getId();
		f.set("faces", txt);
		f.set("type", TopologyAnalysis.getTopologicalType(n));
		return f;
	}

	//edge
	public static Feature asFeature(Edge e){
		Feature f = new Feature();
		f.setGeom(e.getGeometry());
		f.id = e.getId();
		f.set("id", e.getId());
		f.set("value", e.value);
		f.set("n1", e.getN1()!=null? e.getN1().getId() : "null");
		f.set("n2", e.getN2()!=null? e.getN2().getId() : "null");
		f.set("face_1", e.f1!=null?e.f1.getId():null);
		f.set("face_2", e.f2!=null?e.f2.getId():null);
		f.set("coastal", TopologyAnalysis.getCoastalType(e));
		f.set("topo", TopologyAnalysis.getTopologicalType(e));
		return f;
	}

	//face
	public static Feature asFeature(Face face) {
		Feature f = new Feature();

		f.setGeom(face.getGeom());
		if(f.getGeom()==null) {
			LOGGER.warn("NB: null geom for face "+face.getId());
		}
		else if(!f.getGeom().isValid()) {
			LOGGER.warn("NB: non valide geometry for face "+face.getId());
		}

		f.id = face.getId();
		f.set("id", face.getId());
		f.set("value", face.value);
		f.set("edge_nb", face.getEdges().size());
		String txt=null;
		for(Edge e:face.getEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.set("edge", txt);
		f.set("type", TopologyAnalysis.getTopologicalType(face));
		f.set("face_nb", face.getTouchingFaces().size());
		return f;
	}

	//generic
	public static Feature asFeature(GraphElement ge) {
		if(ge instanceof Node) return asFeature((Node)ge);
		else if(ge instanceof Edge) return asFeature((Edge)ge);
		else if(ge instanceof Face) return asFeature((Face)ge);
		else return null;
	}

	//collections
	public static <T extends GraphElement> Collection<Feature> asFeature(Collection<T> ges){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(T ge : ges)
			fs.add(asFeature(ge));
		return fs;
	}






	public static Set<Feature> getEdgeAttachedFeatures(Collection<Edge> es) {
		Set<Feature> out = new HashSet<Feature>();
		for(Edge e : es)
			out.add((Feature) e.obj);
		return out;
	}


	public static void updateEdgeLinearFeatureGeometry(Graph g) {
		//TODO
		//get all features
		//for each feature, get the edges
		//build new geometry from edges
		//check validity
		//set feature new geometry
		//return new features?
	}

}
