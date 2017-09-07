/**
 * 
 */
package org.opencarto.datamodel.graph;

/**
 * @author julien gaffuri
 *
 */
public abstract class GraphElement {

	private Graph graph;
	public Graph getGraph() { return graph; }

	//the id
	private String id;
	public String getId(){ return id; }

	public GraphElement(Graph graph, String id){
		this.graph = graph;
		this.id = id;
	}




	//an object linked to the element
	public Object obj;
	//a value linked to the element
	public double value;


	@Override
	public String toString() {
		return getClass().getSimpleName()+"-"+getId();
	}

}
