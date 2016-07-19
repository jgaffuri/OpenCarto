package org.opencarto.util;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * @author julien Gaffuri
 *
 */
public class XMLUtil {

	public static Document parseXMLfromURL(String urlString){
		try{
			InputStream in = new URL(urlString).openConnection().getInputStream();
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		}
		catch(Exception e){
			e.printStackTrace();
		}       
		return null;
	}

}
