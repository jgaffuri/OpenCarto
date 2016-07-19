/**
 * 
 */
package org.opencarto.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author julien Gaffuri
 *
 */
public class CompressUtil {

	public static void unGZIP(String inGZIPFile, String outUnGZIPFile){
		try {
			GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(inGZIPFile));
			FileOutputStream out = new FileOutputStream(outUnGZIPFile);
			int len;
			byte[] buffer = new byte[1024];
			while ((len = gzis.read(buffer)) > 0)
				out.write(buffer, 0, len);
			gzis.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
