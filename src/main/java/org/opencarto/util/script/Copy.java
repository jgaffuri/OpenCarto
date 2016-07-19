package org.opencarto.util.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Copy {

	//copy content of directory into another one - non recursive
	public static void main(String[] args) {

		File srcDir = new File(args[0]);
		File destDir = new File(args[1]);
		String clean = null;
		if(args.length == 3) clean = args[2];

		if(!srcDir.exists()){
			System.out.println("Source directory " + args[0] + " does not exist.");
			return;
		}
		if(!destDir.exists()){
			System.out.println("Destination directory " + args[1] + " does not exist.");
			return;
		}
		if(!srcDir.isDirectory()){
			System.out.println("Source directory " + args[0] + " is not a directory.");
			return;
		}
		if(!destDir.isDirectory()){
			System.out.println("Destination directory " + args[1] + " is not a directory.");
			return;
		}

		try {
			String files[] = srcDir.list();
			for (String file : files) {
				System.out.println("Copy " + file + " from " + srcDir + " to " + destDir);

				String srcFile = srcDir + System.getProperties().getProperty("file.separator") + file;
				FileInputStream srcIS = new FileInputStream(srcFile);
				String destFile = destDir + System.getProperties().getProperty("file.separator") + file;
				FileOutputStream destOS = new FileOutputStream(destFile); 

				int size;
				byte[] buf = new byte[1024];
				while ((size = srcIS.read(buf)) > 0)
					destOS.write(buf, 0, size);
				srcIS.close();
				destOS.close();

				if(clean != null)
					if(new File(srcFile).delete()) System.out.println(srcFile + " deleted.");
					else System.out.println("Deletion of " + srcFile + " failed");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
