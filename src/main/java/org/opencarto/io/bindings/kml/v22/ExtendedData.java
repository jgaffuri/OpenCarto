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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * <p>Java class for ExtendedDataType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtendedDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Data" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}SchemaData" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtendedDataType", propOrder = {
    "datas",
    "schemaDatas",
    "anies"
})
@XmlRootElement(name = "ExtendedData")
public class ExtendedData {

    @XmlElement(name = "Data")
    protected List<DataType> datas;
    @XmlElement(name = "SchemaData")
    protected List<SchemaDataType> schemaDatas;
    @XmlAnyElement
    protected List<Element> anies;

    /**
     * Gets the value of the datas property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datas property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatas().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataType }
     * 
     * 
     */
    public List<DataType> getDatas() {
        if (datas == null) {
            datas = new ArrayList<DataType>();
        }
        return this.datas;
    }

    /**
     * Gets the value of the schemaDatas property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schemaDatas property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchemaDatas().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemaDataType }
     * 
     * 
     */
    public List<SchemaDataType> getSchemaDatas() {
        if (schemaDatas == null) {
            schemaDatas = new ArrayList<SchemaDataType>();
        }
        return this.schemaDatas;
    }

    /**
     * Gets the value of the anies property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the anies property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnies().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * 
     * 
     */
    public List<Element> getAnies() {
        if (anies == null) {
            anies = new ArrayList<Element>();
        }
        return this.anies;
    }

}
