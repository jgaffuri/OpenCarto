//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.18 at 10:52:24 AM CEST 
//


package org.opencarto.io.bindings.kml.v22;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for UpdateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UpdateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}targetHref"/>
 *         &lt;choice maxOccurs="unbounded">
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Create"/>
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Delete"/>
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Change"/>
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}UpdateOpExtensionGroup"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}UpdateExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateType", propOrder = {
    "targetHref",
    "createsAndDeletesAndChanges",
    "updateExtensionGroups"
})
@XmlRootElement(name = "Update")
public class Update {

    @XmlElement(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String targetHref;
    @XmlElements({
        @XmlElement(name = "UpdateOpExtensionGroup"),
        @XmlElement(name = "Delete", type = Delete.class),
        @XmlElement(name = "Create", type = Create.class),
        @XmlElement(name = "Change", type = Change.class)
    })
    protected List<Object> createsAndDeletesAndChanges;
    @XmlElement(name = "UpdateExtensionGroup")
    protected List<Object> updateExtensionGroups;

    /**
     * Gets the value of the targetHref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetHref() {
        return targetHref;
    }

    /**
     * Sets the value of the targetHref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetHref(String value) {
        this.targetHref = value;
    }

    /**
     * Gets the value of the createsAndDeletesAndChanges property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the createsAndDeletesAndChanges property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCreatesAndDeletesAndChanges().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Delete }
     * {@link Create }
     * {@link Change }
     * 
     * 
     */
    public List<Object> getCreatesAndDeletesAndChanges() {
        if (createsAndDeletesAndChanges == null) {
            createsAndDeletesAndChanges = new ArrayList<Object>();
        }
        return this.createsAndDeletesAndChanges;
    }

    /**
     * Gets the value of the updateExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the updateExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUpdateExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getUpdateExtensionGroups() {
        if (updateExtensionGroups == null) {
            updateExtensionGroups = new ArrayList<Object>();
        }
        return this.updateExtensionGroups;
    }

}
