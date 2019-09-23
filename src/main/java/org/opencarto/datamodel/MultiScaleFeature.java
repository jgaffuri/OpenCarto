/**
 * 
 */
package org.opencarto.datamodel;

import org.locationtech.jts.geom.Geometry;

/**
 * @author Julien Gaffuri
 *
 */
public class MultiScaleFeature extends Feature {

	public Geometry getDefaultGeometry(){ return _getGeomMSP().get(); }
	public void setDefaultGeometry(Geometry geom){ _getGeomMSP().set(geom); }

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
