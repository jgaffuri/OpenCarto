/**
 * 
 */
package org.opencarto.datamodel;

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

/**
 * @author julien Gaffuri
 *
 */
public class Feature {

	//id
	private static int ID;
	private String id;
	public String getID() { return id; }
	public void setID(String id) { this.id = id; }

	public Feature(){
		id = String.valueOf(ID++);
	}

	public Geometry getDefaultGeometry(){ return _getGeomMSP().get(); }
	public Feature setDefaultGeometry(Geometry geom){ _getGeomMSP().set(geom); return this; }

	//attributes
	private Map<String, Object> atts;
	public Map<String, Object> getAttributes(){
		if(atts==null) atts = new HashMap<String, Object>();
		return atts;
	}
	public Object getAttribute(String att) { return getAttributes().get(att); }
	public Object setAttribute(String key, Object value) { return getAttributes().put(key, value); }




	//geometries
	private MultiScaleProperty<Geometry> geoms = null;
	private MultiScaleProperty<Geometry> _getGeomMSP(){
		if(geoms==null) geoms = new MultiScaleProperty<Geometry>();
		return geoms;
	}
	public Geometry getGeom(int z){ Geometry g = _getGeomMSP().get(z); if(g!=null) return g; else return getDefaultGeometry(); }
	public Geometry getGeom(String z) { return _getGeomMSP().get(z); }
	public Feature setGeom(Geometry geom, int z){ _getGeomMSP().set(geom,z); return this; }

}
