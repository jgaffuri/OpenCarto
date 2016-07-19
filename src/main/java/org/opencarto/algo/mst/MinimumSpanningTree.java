/**
 * 
 */
package org.opencarto.algo.mst;

import java.util.ArrayList;
import java.util.Collection;

import org.opencarto.algo.distances.Distance;
import org.opencarto.algo.graph.GraphUnion;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;

/**
 * @author gaffuju
 *
 */
public class MinimumSpanningTree<T> {

	public Graph<T> perform(Collection<T> objs, Distance<T> d) {
		//initialise graphs list
		ArrayList<Graph<T>> graphs = new ArrayList<Graph<T>>();
		for(T obj:objs){
			Graph<T> g = new Graph<T>();
			Node<T> n = g.buildNode();
			n.obj = obj;
			graphs.add(g);
		}

		//build MST
		while(graphs.size()>1){
			//find closest graphs
			Object[] cgs = getClosest(graphs, d);
			//aggregate them
			@SuppressWarnings("unchecked")
			Graph<T> gAg = new GraphUnion<T>().aggregate((Graph<T>)cgs[0], (Graph<T>)cgs[1], (Node<T>)cgs[2], (Node<T>)cgs[3], (Double)cgs[4]);
			//
			graphs.add(gAg);
			graphs.remove(cgs[0]);
			graphs.remove(cgs[1]);
			//System.out.println((double)cgs[4]);
		}
		return graphs.get(0);
	}


	private Object[] getClosest(ArrayList<Graph<T>> graphs, Distance<T> d) {
		Object[] closest = new Object[]{null,null,null,null,Double.MAX_VALUE};
		for(int i=0; i<graphs.size(); i++){
			Graph<T> gi = graphs.get(i);
			for(int j=i+1; j<graphs.size(); j++){
				Object[] dist = distance(gi, graphs.get(j), d);
				if((Double)dist[4]<(Double)closest[4]) closest=dist;
			}
		}
		return closest;
	}

	private Object[] distance(Graph<T> g1, Graph<T> g2, Distance<T> d) {
		double distMin = Double.MAX_VALUE;
		Node<T> n1Min=null, n2Min=null;
		for(Node<T> n1:g1.getNodes()){
			for(Node<T> n2:g2.getNodes()){
				double dist = d.get(n1.obj,n2.obj);
				if(dist<distMin){
					distMin=dist;
					n1Min=n1; n2Min=n2;
				}
			}
		}
		return new Object[]{g1,g2,n1Min,n2Min,distMin};
	}


	/*
	public static void main(String[] args) {
		System.out.println("Start MST...");

		//load shp
		String path="E:/gaffuju/Desktop/data/import/producers_organisations/";
		SHPData data = SHPUtils.loadSHP(path+"fa_producers_organisations.shp");
		//String path="E:/gaffuju/Desktop/data/import/emodnet_partners/out/";
		//SHPData data = SHPUtils.loadSHP(path+"emodnet_partners_proj.shp");

		System.out.println(" " + data.fs.size());

		Graph graphMST = perform(data.fs, new FeatureEuclidianDistance("the_geom"));
		for(Node n:graphMST.getNodesIterator())
			n.c=((Geometry)((SimpleFeature)n.obj).getAttribute("the_geom")).getCoordinate();

		GraphToSHP.exportEdgesAsSHP(graphMST, path, "MSTedges.shp", 3785);
		GraphToSHP.exportNodesAsSHP(graphMST, path, "MSTnodes.shp", 3785);

		System.out.println("Done");
	}
	 */
}
