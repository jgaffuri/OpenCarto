/**
 * 
 */
package org.opencarto.tiling.description;

import eu.europa.ec.eurostat.jgiscotools.datamodel.Feature;

/**
 * @author julien Gaffuri
 *
 */
public interface DescriptionBuilder {
	String getDescription(Feature f);
}
