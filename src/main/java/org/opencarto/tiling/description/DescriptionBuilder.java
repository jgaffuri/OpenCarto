/**
 * 
 */
package org.opencarto.tiling.description;

import org.opencarto.datamodel.Feature;

/**
 * @author julien Gaffuri
 *
 */
public interface DescriptionBuilder {
	String getDescription(Feature f);
}
