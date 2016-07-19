/**
 * 
 */
package org.opencarto.io;

import java.util.ArrayList;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opencarto.datamodel.graph.Edge;
import org.opencarto.datamodel.graph.Graph;
import org.opencarto.datamodel.graph.Node;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author julien Gaffuri
 *
 */
public class GraphSHPUtil {

	public static void exportEdgesAsSHP(Graph<?> g, String outPath, String outFile, int epsg){
		try {
			SimpleFeatureType ft = DataUtilities.createType("ep", "GEOM:LineString:srid="+epsg+",VALUE:Double");
			SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);
			ArrayList<SimpleFeature> out = new ArrayList<SimpleFeature>();
			int id=0;
			for(Edge<?> e:g.getEdges())
				out.add( sfb.buildFeature(""+(id++), new Object[]{e.getGeometry(),e.value}) );
			SHPUtil.saveSHP(ft, out, outPath, outFile);
		} catch (SchemaException e) {
			e.printStackTrace();
		}


	}

	public static void exportNodesAsSHP(Graph<?> g, String outPath, String outFile, int epsg){
		try {
			SimpleFeatureType ft = DataUtilities.createType("ep", "GEOM:Point:srid="+epsg+",VALUE:Double");
			SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);
			ArrayList<SimpleFeature> out = new ArrayList<SimpleFeature>();
			int id=0;
			for(Node<?> n:g.getNodes())
				out.add( sfb.buildFeature(""+(id++), new Object[]{n.getGeometry(),n.value}) );
			SHPUtil.saveSHP(ft, out, outPath, outFile);
		} catch (SchemaException e) {
			e.printStackTrace();
		}
	}

}
