package main.java.liasd.asadera.tools.reader_writer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Reader {

	private String pathFichierRead;
	private boolean readLine;
	private boolean affichage;
	private int size;
	
	FileInputStream fis;
    InputStreamReader isr;
    BufferedReader br;
	
	public Reader() {
	}
	
	public Reader(String pathFichierRead, boolean readLine) {
		this.pathFichierRead = pathFichierRead;
		this.readLine = readLine;
		affichage = false;
	}
	
	public Reader(String pathFichierRead, boolean readLine, boolean affichage) {
		this.pathFichierRead = pathFichierRead;
		this.readLine = readLine;
		this.affichage = affichage;
	}
	
	public void finalize() {
		this.close();
    }
	
	public void open() throws FileNotFoundException, UnsupportedEncodingException, IOException {
		fis = new FileInputStream(pathFichierRead);
	    isr = new InputStreamReader(fis, "UTF-8");
	    br = new BufferedReader (isr);
		size = fis.available();
	}

	public void close() {
		try
	    {
			br.close();
	        isr.close();
	        fis.close();
	    }
	    catch (IOException exception)
	    {
	        System.out.println ("Error while closing : " + exception.getMessage());
	    }
	}

	public String read() throws Exception {
		if (br != null) {
	        String line = br.readLine();
	        String temp = "";
	        while (temp != null && !readLine)
	        {
	            line += temp;
	            temp = br.readLine();
	            if (affichage)
	            	System.out.println(temp);
	        }
	        return line;
		}
		else
			throw new Exception("Reader need to be open first !");
	}
	
	public int size() {
		return size;
	}
	
	public boolean isReadLine() {
		return readLine;
	}

	public void setReadLine(boolean readLine) {
		this.readLine = readLine;
	}
}
