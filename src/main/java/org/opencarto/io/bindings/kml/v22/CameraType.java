//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.18 at 10:52:24 AM CEST 
//


package org.opencarto.io.bindings.kml.v22;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CameraType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CameraType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractViewType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}longitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}latitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}heading" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}tilt" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}roll" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}CameraSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}CameraObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CameraType", propOrder = {
    "longitude",
    "latitude",
    "altitude",
    "heading",
    "tilt",
    "roll",
    "altitudeModeGroup",
    "cameraSimpleExtensionGroups",
    "cameraObjectExtensionGroups"
})
public class CameraType
    extends AbstractViewType
{

    @XmlElement(defaultValue = "0.0")
    protected Double longitude;
    @XmlElement(defaultValue = "0.0")
    protected Double latitude;
    @XmlElement(defaultValue = "0.0")
    protected Double altitude;
    @XmlElement(defaultValue = "0.0")
    protected Double heading;
    @XmlElement(defaultValue = "0.0")
    protected Double tilt;
    @XmlElement(defaultValue = "0.0")
    protected Double roll;
    @XmlElementRef(name = "altitudeModeGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected JAXBElement<?> altitudeModeGroup;
    @XmlElement(name = "CameraSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> cameraSimpleExtensionGroups;
    @XmlElement(name = "CameraObjectExtensionGroup")
    protected List<AbstractObjectType> cameraObjectExtensionGroups;

    /**
     * Gets the value of the longitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets the value of the longitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLongitude(Double value) {
        this.longitude = value;
    }

    /**
     * Gets the value of the latitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Sets the value of the latitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLatitude(Double value) {
        this.latitude = value;
    }

    /**
     * Gets the value of the altitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * Sets the value of the altitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAltitude(Double value) {
        this.altitude = value;
    }

    /**
     * Gets the value of the heading property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHeading() {
        return heading;
    }

    /**
     * Sets the value of the heading property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setHeading(Double value) {
        this.heading = value;
    }

    /**
     * Gets the value of the tilt property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTilt() {
        return tilt;
    }

    /**
     * Sets the value of the tilt property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTilt(Double value) {
        this.tilt = value;
    }

    /**
     * Gets the value of the roll property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRoll() {
        return roll;
    }

    /**
     * Sets the value of the roll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRoll(Double value) {
        this.roll = value;
    }

    /**
     * Gets the value of the altitudeModeGroup property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     
     */
    public JAXBElement<?> getAltitudeModeGroup() {
        return altitudeModeGroup;
    }

    /**
     * Sets the value of the altitudeModeGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     {@link JAXBElement }{@code <}{@link AltitudeModeEnumType }{@code >}
     *     
     */
    public void setAltitudeModeGroup(JAXBElement<?> value) {
        this.altitudeModeGroup = value;
    }

    /**
     * Gets the value of the cameraSimpleExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cameraSimpleExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCameraSimpleExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getCameraSimpleExtensionGroups() {
        if (cameraSimpleExtensionGroups == null) {
            cameraSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.cameraSimpleExtensionGroups;
    }

    /**
     * Gets the value of the cameraObjectExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cameraObjectExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCameraObjectExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getCameraObjectExtensionGroups() {
        if (cameraObjectExtensionGroups == null) {
            cameraObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.cameraObjectExtensionGroups;
    }

}
