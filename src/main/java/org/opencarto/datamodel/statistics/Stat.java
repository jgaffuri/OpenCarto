/**
 * 
 */
package org.opencarto.datamodel.statistics;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author julien Gaffuri
 *
 */
public class Stat {

	//dim label - dim value
	public HashMap<String,String> dims = new HashMap<String,String>();

	//
	public double value;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Entry<String,String> dim:dims.entrySet())
			sb.append(dim.getKey()).append(":").append(dim.getValue()).append(", ");
		sb.append(value);
		return sb.toString();
	}

}
