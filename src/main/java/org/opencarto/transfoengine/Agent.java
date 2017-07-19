/**
 * 
 */
package org.opencarto.transfoengine;

/**
 * @author julien Gaffuri
 *
 */
public class Agent {

	public Agent(Object object){
		this.object=object;
	}

	private Object object;
	public Object getObject() { return object; }

	private boolean deleted = false;
	public boolean isDeleted() { return deleted; }
	public void setDeleted(boolean deleted) { this.deleted = deleted; }


}
