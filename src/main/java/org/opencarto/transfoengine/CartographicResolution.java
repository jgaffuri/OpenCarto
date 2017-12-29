/**
 * 
 */
package org.opencarto.transfoengine;

/**
 * Threshold parameters for cartographic representations
 * 
 * @author julien Gaffuri
 *
 */
public class CartographicResolution {
	
	private double perceptionFilledPointSizeM;
	private double perceptionFilledSquareSizeM;
	private double perceptionPointSizeM;
	private double perceptionSquareSizeM;
	private double perceptionLineSizeM;
	private double perceptionSizeSqMeter;
	private double separationDistanceMeter;

	/**
	 * @param scaleDenominator The scale denominator. Ex: 1e6 for 1:1M scale.
	 */
	public CartographicResolution(int scaleDenominator) {
		//resolution is 0.1mm map. 0.1mm at 1:1M -> 1e-4*1e6 = 1e2 = 100m
		double res = scaleDenominator*1e-4;

		//0.2mm
		perceptionFilledPointSizeM = 2*res;
		//0.4mm
		perceptionFilledSquareSizeM = 4*res;
		//0.3mm
		perceptionPointSizeM = 3*res;
		//0.5mm
		perceptionSquareSizeM = 5*res;
		//0.1mm
		perceptionLineSizeM = 1*res;

		perceptionSizeSqMeter = x * res*res;

		//0.2mm
		separationDistanceMeter = 2*res;
	}
	
}
