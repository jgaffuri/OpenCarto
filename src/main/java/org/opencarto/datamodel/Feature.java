/**
 * 
 */
package org.opencarto.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author juju
 *
 */
public class Feature{

	//id
	private static int ID;
	public String id;

	//geometries
	public static String GEOM = "the_geom";
	private Map<String, Geometry> geoms = new HashMap<String, Geometry>();

	public Geometry getGeom(){ return geoms.get(GEOM); }
	public void setGeom(Geometry geom){ geoms.put(GEOM,geom); }
	public Geometry getGeom(int z){ return (Geometry) geoms.get(GEOM+z); }
	public Geometry getGeom(String z) {if(z==null || z=="") return getGeom(); else return getGeom(Integer.parseInt(z));}
	public void setGeom(Geometry geom, int z){ geoms.put(GEOM+z,geom); }

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
