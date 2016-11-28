/**
 * 
 */
package org.opencarto.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.opencarto.style.Style;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class Feature{

	//id
	private static int ID;
	public String id;

	//geometries
	private MultiScaleProperty<Geometry> geoms = new MultiScaleProperty<Geometry>();
	public Geometry getGeom(){ return geoms.get(); }
	public void setGeom(Geometry geom){ geoms.set(geom); }
	public Geometry getGeom(int z){ return geoms.get(z); }
	public Geometry getGeom(String z) { return geoms.get(z); }
	public void setGeom(Geometry geom, int z){ geoms.set(geom,z); }

	//styles
	private MultiScaleProperty<Style> styles = new MultiScaleProperty<Style>();
	public Style getStyle(){ return styles.get(); }
	public void setStyle(Style style){ styles.set(style); }
	public Style getStyle(int z){ return styles.get(z); }
	public Style getStyle(String z) { return styles.get(z); }
	public void setStyle(Style style, int z){ styles.set(style,z); }

	//properties
	public Map<String, Object> props = new HashMap<String, Object>();

	//projection code
	private int projCode = -1;
	public int getProjCode(){ return projCode; }
	public void setProjCode(int projCode){ this.projCode = projCode; }


	public Feature(){
		id = String.valueOf(ID++);
	}

	private ArrayList<Feature> components = null;
	public ArrayList<Feature> getComponents() {
		if(components==null) components = new ArrayList<Feature>();
		return components;
	}
}
