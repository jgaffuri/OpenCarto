/**
 * 
 */
package org.opencarto.style;

import java.util.HashMap;
import java.util.Map;

import org.opencarto.style.basic.BasicStyle;

/**
 * @author julien Gaffuri
 *
 */
public class MultiScaleStyle {
	private static String STYLE = "style";
	private Map<String, Style> styles = new HashMap<String, Style>();

	//default style. to use when same style for all scales
	public Style getStyle(){ return styles.get(STYLE); }
	public MultiScaleStyle setStyle(Style st){ styles.put(STYLE,st); return this; }

	//return the style of the zoom level if it exists, otherwise the default style
	public Style getStyle(int z){
		Style st = styles.get(STYLE+z);
		if(st!=null) return st;
		return getStyle();
	}
	public Style getStyle(String z) {if(z==null || z=="") return getStyle(); else return getStyle(Integer.parseInt(z));}
	public MultiScaleStyle setStyle(Style st, int z){ styles.put(STYLE+z,st); return this; }
	public MultiScaleStyle setStyle(Style st, int zMin, int zMax){
		for(int z=zMin; z<=zMax; z++) setStyle(st,z);
		return this;
	}

	public MultiScaleStyle(){ setStyle(new BasicStyle()); }
	public MultiScaleStyle(Style st){ setStyle(st); }

}
