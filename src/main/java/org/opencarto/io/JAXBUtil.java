package org.opencarto.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class JAXBUtil {

	//JAXB unmarshaller
	public static Object getUnmarshalledObject(File file, Class<?> cl) {
		try {
			FileInputStream ips = new FileInputStream(file);
			Unmarshaller u = JAXBContext.newInstance( cl.getPackage().getName() ).createUnmarshaller();
			Object obj = u.unmarshal(ips);
			try { ips.close(); } catch (IOException e) { e.printStackTrace(); }
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
