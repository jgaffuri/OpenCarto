/**
 * 
 */
package org.opencarto.tiling.description;

import java.util.Map.Entry;

import eu.europa.ec.eurostat.jgiscotools.feature.Feature;

/**
 * 
 * 
 * @author julien Gaffuri
 *
 */
public class DefaultDescriptionBuilder implements DescriptionBuilder {

	//Return the properties of the feature
	public String getDescription(Feature f) {
		StringBuffer sb = new StringBuffer();

		for(Entry<String, Object> e : f.getAttributes().entrySet()){
			if(e.getValue()==null) continue;
			if("".equals(e.getValue())) continue;
			if(" ".equals(e.getValue())) continue;
			if("  ".equals(e.getValue())) continue;
			sb
			.append("<b>")
			.append(e.getKey())
			.append("</b>: ")
			.append(e.getValue())
			.append("<br>")
			;
		}

		return sb.toString();
	}

}
