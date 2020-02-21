/**
 * 
 */
package org.opencarto;

import org.locationtech.jts.geom.Geometry;

import eu.europa.ec.eurostat.jgiscotools.feature.Feature;

/**
 * @author Julien Gaffuri
 *
 */
public class MultiScaleFeature extends Feature {

	@Override
	public Geometry getGeometry(){ return _getGeomMSP().get(); }
	@Override
	public void setGeometry(Geometry geom){ _getGeomMSP().set(geom); }

	//geometries
	private MultiScaleProperty<Geometry> geoms = null;
	private MultiScaleProperty<Geometry> _getGeomMSP(){
		if(geoms==null) geoms = new MultiScaleProperty<Geometry>();
		return geoms;
	}
	public Geometry getGeom(int z){ Geometry g = _getGeomMSP().get(z); if(g!=null) return g; else return getGeometry(); }
	public Geometry getGeom(String z) { return _getGeomMSP().get(z); }
	public Feature setGeom(Geometry geom, int z){ _getGeomMSP().set(geom,z); return this; }

}
