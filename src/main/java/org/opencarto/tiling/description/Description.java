/**
 * 
 */
package org.opencarto.tiling.description;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;

import org.opencarto.datamodel.Feature;

/**
 * @author julien Gaffuri
 *
 */
public class Description {
	private static final String HTML=".html";

	public static void export(Collection<? extends Feature> fs, String outPath, DescriptionBuilder db, boolean clearProps) {
		new File(outPath+File.separator+"p").mkdirs();
		for(Feature f : fs) {
			String desc = db.getDescription(f);
			if(clearProps) f.getAttributes().clear();
			if(desc == null || desc == "")
				continue;

			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(new File(outPath + File.separator + "p" + File.separator + f.getID() + HTML)));
				out.write(desc);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
