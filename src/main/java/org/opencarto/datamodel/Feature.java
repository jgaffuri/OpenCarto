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
	public Feature setGeom(Geometry geom){ geoms.set(geom); return this; }
	public Geometry getGeom(int z){ return geoms.get(z); }
	public Geometry getGeom(String z) { return geoms.get(z); }
	public Feature setGeom(Geometry geom, int z){ geoms.set(geom,z); return this; }

	//styles
	private MultiScaleProperty<Style<Feature>> styles = new MultiScaleProperty<Style<Feature>>();
	public Style<Feature> getStyle(){ return styles.get(); }
	public Feature setStyle(Style<Feature> style){ styles.set(style); return this; }
	public Style<Feature> getStyle(int z){ return styles.get(z); }
	public Style<Feature> getStyle(String z) { return styles.get(z); }
	public Feature setStyle(Style<Feature> style, int z){ styles.set(style,z); return this; }

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
