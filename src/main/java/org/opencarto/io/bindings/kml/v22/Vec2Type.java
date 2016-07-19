//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.18 at 10:52:24 AM CEST 
//


package org.opencarto.io.bindings.kml.v22;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for vec2Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="vec2Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="x" type="{http://www.w3.org/2001/XMLSchema}double" default="1.0" />
 *       &lt;attribute name="y" type="{http://www.w3.org/2001/XMLSchema}double" default="1.0" />
 *       &lt;attribute name="xunits" type="{http://www.opengis.net/kml/2.2}unitsEnumType" default="fraction" />
 *       &lt;attribute name="yunits" type="{http://www.opengis.net/kml/2.2}unitsEnumType" default="fraction" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vec2Type")
public class Vec2Type {

	@XmlAttribute
	protected Double x;
	@XmlAttribute
	protected Double y;
	@XmlAttribute
	protected UnitsEnumType xunits;
	@XmlAttribute
	protected UnitsEnumType yunits;

	/**
	 * Gets the value of the x property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Double }
	 *     
	 */
	public double getX() {
		if (x == null) {
			return  1.0D;
		}
		return x.doubleValue();
	}

	/**
	 * Sets the value of the x property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Double }
	 *     
	 */
	public void setX(Double value) {
		this.x = value;
	}

	/**
	 * Gets the value of the y property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link Double }
	 *     
	 */
	public double getY() {
		if (y == null) {
			return  1.0D;
		}
		return y.doubleValue();
	}

	/**
	 * Sets the value of the y property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link Double }
	 *     
	 */
	public void setY(Double value) {
		this.y = value;
	}

	/**
	 * Gets the value of the xunits property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link UnitsEnumType }
	 *     
	 */
	public UnitsEnumType getXunits() {
		if (xunits == null) {
			return UnitsEnumType.FRACTION;
		}
		return xunits;
	}

	/**
	 * Sets the value of the xunits property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link UnitsEnumType }
	 *     
	 */
	public void setXunits(UnitsEnumType value) {
		this.xunits = value;
	}

	/**
	 * Gets the value of the yunits property.
	 * 
	 * @return
	 *     possible object is
	 *     {@link UnitsEnumType }
	 *     
	 */
	public UnitsEnumType getYunits() {
		if (yunits == null) {
			return UnitsEnumType.FRACTION;
		}
		return yunits;
	}

	/**
	 * Sets the value of the yunits property.
	 * 
	 * @param value
	 *     allowed object is
	 *     {@link UnitsEnumType }
	 *     
	 */
	public void setYunits(UnitsEnumType value) {
		this.yunits = value;
	}

}
