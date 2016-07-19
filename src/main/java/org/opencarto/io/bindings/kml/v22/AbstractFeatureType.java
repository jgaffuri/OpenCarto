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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.opencarto.io.bindings.atom.Author;
import org.opencarto.io.bindings.atom.Link;
import org.opencarto.io.bindings.xal.AddressDetails;


/**
 * <p>Java class for AbstractFeatureType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractFeatureType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractObjectType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}visibility" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}open" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2005/Atom}author" minOccurs="0"/>
 *         &lt;element ref="{http://www.w3.org/2005/Atom}link" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}address" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}AddressDetails" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}phoneNumber" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Snippet" minOccurs="0"/>
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}snippet" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractViewGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractTimePrimitiveGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}styleUrl" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractStyleSelectorGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}Region" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}Metadata" minOccurs="0"/>
 *           &lt;element ref="{http://www.opengis.net/kml/2.2}ExtendedData" minOccurs="0"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractFeatureObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractFeatureType", propOrder = {
    "name",
    "visibility",
    "open",
    "author",
    "atomLink",
    "address",
    "addressDetails",
    "phoneNumber",
    "snippet",
    "snippetDeprecated",
    "description",
    "abstractViewGroup",
    "abstractTimePrimitiveGroup",
    "styleUrl",
    "abstractStyleSelectorGroups",
    "region",
    "extendedData",
    "metadata",
    "abstractFeatureSimpleExtensionGroups",
    "abstractFeatureObjectExtensionGroups"
})
@XmlSeeAlso({
    NetworkLinkType.class,
    AbstractOverlayType.class,
    AbstractContainerType.class,
    PlacemarkType.class
})
public abstract class AbstractFeatureType
    extends AbstractObjectType
{

    protected String name;
    @XmlElement(defaultValue = "1")
    protected Boolean visibility;
    @XmlElement(defaultValue = "0")
    protected Boolean open;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    protected Author author;
    @XmlElement(name = "link", namespace = "http://www.w3.org/2005/Atom")
    protected Link atomLink;
    protected String address;
    @XmlElement(name = "AddressDetails", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected AddressDetails addressDetails;
    protected String phoneNumber;
    protected String snippet;
    @XmlElement(name = "Snippet")
    protected SnippetType snippetDeprecated;
    protected String description;
    @XmlElementRef(name = "AbstractViewGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractViewType> abstractViewGroup;
    @XmlElementRef(name = "AbstractTimePrimitiveGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected JAXBElement<? extends AbstractTimePrimitiveType> abstractTimePrimitiveGroup;
    @XmlSchemaType(name = "anyURI")
    protected String styleUrl;
    @XmlElementRef(name = "AbstractStyleSelectorGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractStyleSelectorType>> abstractStyleSelectorGroups;
    @XmlElement(name = "Region")
    protected RegionType region;
    @XmlElement(name = "ExtendedData")
    protected ExtendedData extendedData;
    @XmlElement(name = "Metadata")
    protected Metadata metadata;
    @XmlElement(name = "AbstractFeatureSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> abstractFeatureSimpleExtensionGroups;
    @XmlElement(name = "AbstractFeatureObjectExtensionGroup")
    protected List<AbstractObjectType> abstractFeatureObjectExtensionGroups;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVisibility(Boolean value) {
        this.visibility = value;
    }

    /**
     * Gets the value of the open property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOpen() {
        return open;
    }

    /**
     * Sets the value of the open property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOpen(Boolean value) {
        this.open = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link Author }
     *     
     */
    public Author getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link Author }
     *     
     */
    public void setAuthor(Author value) {
        this.author = value;
    }

    /**
     * Gets the value of the atomLink property.
     * 
     * @return
     *     possible object is
     *     {@link Link }
     *     
     */
    public Link getAtomLink() {
        return atomLink;
    }

    /**
     * Sets the value of the atomLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link Link }
     *     
     */
    public void setAtomLink(Link value) {
        this.atomLink = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddress(String value) {
        this.address = value;
    }

    /**
     * Gets the value of the addressDetails property.
     * 
     * @return
     *     possible object is
     *     {@link AddressDetails }
     *     
     */
    public AddressDetails getAddressDetails() {
        return addressDetails;
    }

    /**
     * Sets the value of the addressDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressDetails }
     *     
     */
    public void setAddressDetails(AddressDetails value) {
        this.addressDetails = value;
    }

    /**
     * Gets the value of the phoneNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the value of the phoneNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    /**
     * Gets the value of the snippet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSnippet() {
        return snippet;
    }

    /**
     * Sets the value of the snippet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSnippet(String value) {
        this.snippet = value;
    }

    /**
     * Gets the value of the snippetDeprecated property.
     * 
     * @return
     *     possible object is
     *     {@link SnippetType }
     *     
     */
    public SnippetType getSnippetDeprecated() {
        return snippetDeprecated;
    }

    /**
     * Sets the value of the snippetDeprecated property.
     * 
     * @param value
     *     allowed object is
     *     {@link SnippetType }
     *     
     */
    public void setSnippetDeprecated(SnippetType value) {
        this.snippetDeprecated = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the abstractViewGroup property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractViewType> getAbstractViewGroup() {
        return abstractViewGroup;
    }

    /**
     * Sets the value of the abstractViewGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link CameraType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractViewType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     *     
     */
    public void setAbstractViewGroup(JAXBElement<? extends AbstractViewType> value) {
        this.abstractViewGroup = value;
    }

    /**
     * Gets the value of the abstractTimePrimitiveGroup property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link TimeSpanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TimeStampType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}
     *     
     */
    public JAXBElement<? extends AbstractTimePrimitiveType> getAbstractTimePrimitiveGroup() {
        return abstractTimePrimitiveGroup;
    }

    /**
     * Sets the value of the abstractTimePrimitiveGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link TimeSpanType }{@code >}
     *     {@link JAXBElement }{@code <}{@link TimeStampType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractTimePrimitiveType }{@code >}
     *     
     */
    public void setAbstractTimePrimitiveGroup(JAXBElement<? extends AbstractTimePrimitiveType> value) {
        this.abstractTimePrimitiveGroup = value;
    }

    /**
     * Gets the value of the styleUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyleUrl() {
        return styleUrl;
    }

    /**
     * Sets the value of the styleUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyleUrl(String value) {
        this.styleUrl = value;
    }

    /**
     * Gets the value of the abstractStyleSelectorGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractStyleSelectorGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractStyleSelectorGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link StyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleMapType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractStyleSelectorType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractStyleSelectorType>> getAbstractStyleSelectorGroups() {
        if (abstractStyleSelectorGroups == null) {
            abstractStyleSelectorGroups = new ArrayList<JAXBElement<? extends AbstractStyleSelectorType>>();
        }
        return this.abstractStyleSelectorGroups;
    }

    /**
     * Gets the value of the region property.
     * 
     * @return
     *     possible object is
     *     {@link RegionType }
     *     
     */
    public RegionType getRegion() {
        return region;
    }

    /**
     * Sets the value of the region property.
     * 
     * @param value
     *     allowed object is
     *     {@link RegionType }
     *     
     */
    public void setRegion(RegionType value) {
        this.region = value;
    }

    /**
     * Gets the value of the extendedData property.
     * 
     * @return
     *     possible object is
     *     {@link ExtendedData }
     *     
     */
    public ExtendedData getExtendedData() {
        return extendedData;
    }

    /**
     * Sets the value of the extendedData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtendedData }
     *     
     */
    public void setExtendedData(ExtendedData value) {
        this.extendedData = value;
    }

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Metadata }
     *     
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link Metadata }
     *     
     */
    public void setMetadata(Metadata value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the abstractFeatureSimpleExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractFeatureSimpleExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractFeatureSimpleExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAbstractFeatureSimpleExtensionGroups() {
        if (abstractFeatureSimpleExtensionGroups == null) {
            abstractFeatureSimpleExtensionGroups = new ArrayList<Object>();
        }
        return this.abstractFeatureSimpleExtensionGroups;
    }

    /**
     * Gets the value of the abstractFeatureObjectExtensionGroups property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the abstractFeatureObjectExtensionGroups property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAbstractFeatureObjectExtensionGroups().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getAbstractFeatureObjectExtensionGroups() {
        if (abstractFeatureObjectExtensionGroups == null) {
            abstractFeatureObjectExtensionGroups = new ArrayList<AbstractObjectType>();
        }
        return this.abstractFeatureObjectExtensionGroups;
    }

}
