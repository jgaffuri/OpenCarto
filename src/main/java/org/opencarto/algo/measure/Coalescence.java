/**
 * 
 */
package org.opencarto.algo.measure;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * 
 * @author julien Gaffuri
 *
 */
public class Coalescence {

	// 0: no coalescence, 1: strong coalescence
	public static double get(LineString ls, double lineSymbolWidthM) {
		double symbolArea = BufferOp.bufferOp(ls, lineSymbolWidthM*0.5, 5, BufferParameters.CAP_FLAT).getArea();
		double straightSymbolArea = lineSymbolWidthM * ls.getLength();
		double coal = (straightSymbolArea - symbolArea) / straightSymbolArea;
		if(coal<0) return 0;
		return coal;
	}

	public static double get(LineString ls, double lineSymbolWidthMM, double scale) {
		return get(ls, lineSymbolWidthMM * scale * 0.001);
	}

}
