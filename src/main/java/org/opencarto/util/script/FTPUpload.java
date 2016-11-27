package org.opencarto.util.script;

import org.apache.commons.net.ftp.FTP;
import org.opencarto.util.FTPConnection;

public class FTPUpload {

	public static void main(String[] args) {
		String host = args[0];
		String user = args[1];
		String password = args[2];
		String in = args[3];
		String outPath = args[4];

		int bufferSize = -1;
		if(args.length==5) bufferSize = Integer.parseInt(args[5]);

		int fileType = FTP.ASCII_FILE_TYPE;
		if(args.length==6) fileType = FTP.BINARY_FILE_TYPE;

		FTPConnection ftpUploader = new FTPConnection(host, user, password, bufferSize, fileType);
		ftpUploader.upload(in, outPath);
		ftpUploader.disconnect();
		System.out.println("Done");
	}

}
