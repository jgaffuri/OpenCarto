/**
 * 
 */
package org.opencarto.algo.graph;

import java.util.Collection;
import java.util.HashSet;

import org.opencarto.datamodel.Feature;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Face;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author julien Gaffuri
 *
 */
public class GraphToFeature {



	//node
	public static Feature toFeature(Node n){
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
		f.set("type", n.getType());
		return f;
	}

	public static Collection<Feature> getNodeFeatures(Collection<Node> ns){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(Node n:ns)
			fs.add(toFeature(n));
		return fs;		
	}

	public static Collection<Feature> getNodeFeatures(Graph g){ return getNodeFeatures(g.getNodes()); }




	//edge
	public static Feature toFeature(Edge e){
		Feature f = new Feature();
		f.setGeom(e.getGeometry());
		f.id=e.getId();
		f.set("id", e.getId());
		f.set("value", e.value);
		f.set("n1", e.getN1().getId());
		f.set("n2", e.getN2().getId());
		f.set("face_1", e.f1!=null?e.f1.getId():null);
		f.set("face_2", e.f2!=null?e.f2.getId():null);
		f.set("coastal", e.getCoastalType());
		f.set("topo", e.getTopologicalType());
		return f;
	}

	public static Collection<Feature> getEdgeFeatures(Collection<Edge> es){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(Edge e:es)
			fs.add(toFeature(e));
		return fs;		
	}

	public static Collection<Feature> getEdgeFeatures(Graph g){ return getEdgeFeatures(g.getEdges()); }




	//face
	public static Feature toFeature(Face face) {
		Feature f = new Feature();
		f.setGeom(face.getGeom());
		f.id=face.getId();
		f.set("id", face.getId());
		f.set("value", face.value);
		f.set("edge_nb", face.getEdges().size());
		String txt=null;
		for(Edge e:face.getEdges()) txt=(txt==null?"":txt+";")+e.getId();
		f.set("edge", txt);
		f.set("type", face.getType());
		f.set("face_nb", face.getTouchingFaces().size());
		return f;
	}

	public static Collection<Feature> getFaceFeatures(Collection<Face> fss){
		HashSet<Feature> fs = new HashSet<Feature>();
		for(Face face:fss) {
			Feature f = toFeature(face);
			if(f.getGeom()==null){
				System.out.println("NB: null geom for face "+face.getId());
				continue;
			}
			if(!f.getGeom().isValid()) {
				System.out.println("NB: non valide geometry for face "+face.getId());
				continue;
			}
			fs.add(f);
		}
		return fs;
	}

	public static Collection<Feature> getFaceFeatures(Graph g){ return getFaceFeatures(g.getFaces()); }

}
