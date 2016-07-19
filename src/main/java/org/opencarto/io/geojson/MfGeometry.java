package org.opencarto.io.geojson;
/*
 * Copyright (C) 2008  Camptocamp
 *
 * This file is part of MapFish Server
 *
 * MapFish Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MapFish Server.  If not, see <http://www.gnu.org/licenses/>.
 */



import com.vividsolutions.jts.geom.Geometry;

/**
 *
 * @author Eric Lemoine, Camptocamp.
 */
public class MfGeometry implements MfGeo {
	private final Geometry jtsGeometry;

	/**
	 * Creates a new instance of MfGeometry
	 */
	public MfGeometry(Geometry jtsGeometry) {
		this.jtsGeometry = jtsGeometry;
	}

	@Override
	public GeoType getGeoType() {
		return GeoType.GEOMETRY;
	}

	public Geometry getInternalGeometry() {
		return jtsGeometry;
	}
}
