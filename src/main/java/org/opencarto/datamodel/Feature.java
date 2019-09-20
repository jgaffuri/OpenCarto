/**
 * 
 */
package org.opencarto.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.opencarto.style.Style;

/**
 * @author julien Gaffuri
 *
 */
public class Feature {

	//id
	private static int ID;
	private String id;
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	//geometries
	private MultiScaleProperty<Geometry> geoms = null;
	private MultiScaleProperty<Geometry> _getGeomMSP(){
		if(geoms==null) geoms = new MultiScaleProperty<Geometry>();
		return geoms;
	}
	public Geometry getGeom(){ return _getGeomMSP().get(); }
	public Feature setGeom(Geometry geom){ _getGeomMSP().set(geom); return this; }
	public Geometry getGeom(int z){ Geometry g = _getGeomMSP().get(z); if(g!=null) return g; else return getGeom(); }
	public Geometry getGeom(String z) { return _getGeomMSP().get(z); }
	public Feature setGeom(Geometry geom, int z){ _getGeomMSP().set(geom,z); return this; }

	//styles
	private MultiScaleProperty<Style<Feature>> styles = null;
	private MultiScaleProperty<Style<Feature>> _getStyleMSP(){
		if(styles==null) styles = new MultiScaleProperty<Style<Feature>>();
		return styles;
	}
	public Style getStyle(){ return _getStyleMSP().get(); }
	public Feature setStyle(Style<Feature> style){ _getStyleMSP().set(style); return this; }
	public Style getStyle(int z){ Style s = _getStyleMSP().get(z); if(s!=null) return s; else return getStyle(); }
	public Style getStyle(String z) { return _getStyleMSP().get(z); }
	public Feature setStyle(Style<Feature> style, int z){ _getStyleMSP().set(style,z); return this; }

	//properties
	private Map<String, Object> props;
	public Map<String, Object> getProperties(){
		if(props==null) props = new HashMap<String, Object>();
		return props;
	}
	public Object get(String prop) { return getProperties().get(prop); }
	public Object set(String key, Object value) { return getProperties().put(key, value); }


	/*/projection code
	private int projCode = -1;
	public int getProjCode(){ return projCode; }
	public void setProjCode(int projCode){ this.projCode = projCode; }*/


	public Feature(){
		id = String.valueOf(ID++);
	}

	private ArrayList<Feature> components = null;
	public ArrayList<Feature> getComponents() {
		if(components==null) components = new ArrayList<Feature>();
		return components;
	}

}
