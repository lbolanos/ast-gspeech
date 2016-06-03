package com.astexample;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.asteriskjava.fastagi.command.AgiCommand;
import org.asteriskjava.fastagi.command.RecordFileCommand;
import org.asteriskjava.fastagi.command.SayDigitsCommand;
import org.asteriskjava.fastagi.command.StreamFileCommand;
import org.asteriskjava.fastagi.command.VerboseCommand;

/**
 * Hello world!
 *
 */
public class TestApp {
	
	private static LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
	
	public static void execute(AgiCommand command) throws IOException {
		System.out.println(command.buildCommand());
		checkresults();
	}

	public static void readAGIEnv( ) throws IOException {
		String linea;		
		do { // read AGI environment
			linea = in.readLine();
			System.err.println(linea);
		} while (linea.length() > 0);
	}

	public static void checkresults(   ) throws IOException {
		String linea = in.readLine();
		System.err.println("returned:" + linea);
	}
	
	@SuppressWarnings("restriction")
	public static void readMedia() throws IOException {
		FileDescriptor fd = new FileDescriptor();
		sun.misc.SharedSecrets.getJavaIOFileDescriptorAccess().set(fd,3);
		FileInputStream fin = new FileInputStream(fd);
		
		
		String strDestinationFile = "filedata.gsm";
		// create FileOutputStream object for destination file
		FileOutputStream fout = new FileOutputStream(strDestinationFile);

		byte[] b = new byte[1024];
		int noOfBytes = 0;
		int total = 0;

		// System.out.println("Copying file using streams");

		// read bytes from source file and write to destination file
		while ((noOfBytes = fin.read(b)) != -1 && total <= 80000) {
			fout.write(b, 0, noOfBytes);
			total += noOfBytes;
		}

		//System.out.println("File copied!");

		// close the streams
		fin.close();
		fout.close();		
	}

	public static void main(String[] args) {
		try {
			readAGIEnv( );			
			execute( new VerboseCommand("TestApp 2", 1) ); 
			execute( new StreamFileCommand("filedata"));
			execute( new SayDigitsCommand("12354") ); 
			execute( new VerboseCommand("Recording", 1) ); 
			execute( new RecordFileCommand("testwavgsm","gsm","#", 5000 ) );
			execute( new VerboseCommand("Playing", 1) ); 
			execute( new StreamFileCommand("testwavgsm") );
			readMedia();

		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
	}
}
