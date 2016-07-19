package org.opencarto.util.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;


public class FTPUpload {
	private static final String sep = System.getProperties().getProperty("file.separator");

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

		FTPUpload ftpUploader = new FTPUpload(host, user, password, bufferSize, fileType);
		ftpUploader.upload(in, outPath);
		ftpUploader.disconnect();
		System.out.println("Done");
	}

	private FTPClient ftp = null;
	private String host,user,pwd;
	private int bufferSize=-1,fileType=FTP.ASCII_FILE_TYPE;

	public FTPUpload(String host, String user, String pwd, int bufferSize, int fileType) {
		this.host = host;
		this.user = user;
		this.pwd = pwd;
		this.bufferSize = bufferSize;
		this.fileType = fileType;
		ftp = new FTPClient();
		connect();
	}

	public void upload(String in, String outPath) {
		File inFile = new File(in);
		if(!inFile.exists()){
			System.out.println("Source " + in + " does not exist.");
			return;
		}
		if(inFile.isDirectory()){
			String elts[] = inFile.list();
			for (String elt : elts) {
				File eltFile = new File(in + sep + elt);
				if(eltFile.isDirectory()){
					System.out.println("Upload directory " + outPath + sep + elt);
					makeDirectory(outPath + sep + elt);
					upload(in + sep + elt, outPath + sep + elt);
				} else {
					upload(in + sep + elt, outPath);
				}
			}
		} else {
			System.out.println("Upload "+inFile+" to "+outPath);
			storeFile(in, outPath + sep + inFile.getName());
		}
	}

	public void makeDirectory(String outPath){
		try {
			ftp.makeDirectory(outPath);
		} catch (Exception e) {
			//if ftp has been disconnected, reconnect and retry
			if(!ftp.isConnected()) {
				connect();
				makeDirectory(outPath);
				return;
			}
			e.printStackTrace();
		}
	}

	public void storeFile(String inFile, String outFile){
		//java.net.ConnectException
		//org.apache.commons.net.ftp.FTPConnectionClosedException: FTP response 421 received.  Server closed connection.
		try {
			FileInputStream is = new FileInputStream(new File(inFile));
			ftp.storeFile(outFile, is);
			is.close();
		} catch (Exception e) {
			//if ftp has been disconnected, reconnect and retry
			if(!ftp.isConnected()) {
				connect();
				storeFile(inFile, outFile);
				return;
			}
			e.printStackTrace();
		}
	}



	private void connect() {
		if(ftp.isConnected()) return;
		try {
			ftp.connect(host);
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.out.println("Problem in connecting to FTP Server");
			}
			ftp.login(user, pwd);
			ftp.setFileType(fileType);
			ftp.enterLocalPassiveMode();
			if(bufferSize>0) ftp.setBufferSize(bufferSize);
			ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect(){
		if (!ftp.isConnected()) return;
		try {
			ftp.logout();
			ftp.disconnect();
		} catch (Exception f) {}
	}



	/*public static int BUFFER_SIZE = 1024;

	public static void main(String[] args) {
		String ftpUrlBase = "ftp://%s:%s@%s/%s;type=i";
		String host = args[0];
		String user = args[1];
		String password = args[2];
		String inPath = args[3];
		String outPath = args[4];

		upload(ftpUrlBase, host, user, password, inPath, outPath);
		System.out.println("Done");
	}

	private static void upload(String ftpUrlBase, String host, String user, String password, String inPath, String outPath) {
		File src = new File(inPath);
		if(!src.exists()){
			System.out.println("Source " + inPath + " does not exist.");
			return;
		}

		if(!src.isDirectory()){
			try {
				String ftpUrl = String.format(ftpUrlBase, user, password, host, outPath + System.getProperties().getProperty("file.separator") + src.getName());
				System.out.println("Upload to " + ftpUrl);

				FileInputStream is = new FileInputStream(inPath);
				OutputStream os = new URL(ftpUrl).openConnection().getOutputStream();

				byte[] buf = new byte[BUFFER_SIZE];
				int bytesRead = -1;
				while ((bytesRead = is.read(buf)) != -1)
					os.write(buf, 0, bytesRead);
				is.close();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("Directory!");
		}
	}
	 */
}
