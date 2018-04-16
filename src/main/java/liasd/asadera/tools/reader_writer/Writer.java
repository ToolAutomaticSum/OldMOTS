package main.java.liasd.asadera.tools.reader_writer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Writer {
	private String pathFichierWrite;
	private OutputStreamWriter output;
	
	public Writer() {
	}
	
	public Writer(String pathFichierWrite) {
		this.pathFichierWrite = pathFichierWrite;
	}
	
	public void delete(String pathFichierDelete) {
		File deleteOld = new File(pathFichierDelete);
		deleteOld.delete();
	}
	
	public void open(boolean append) throws IOException {
		if (!pathFichierWrite.isEmpty())
			open(pathFichierWrite, append);
		else
			throw new NullPointerException("File path is empty.");
	}
	
	public void open(String path, boolean append) throws IOException {
		File f = new File(path);
		if (f.getParentFile() != null && !f.getParentFile().exists())
			f.getParentFile().mkdirs();
		OutputStream fout= new FileOutputStream(f, append);
        OutputStream bout= new BufferedOutputStream(fout);
        output = new OutputStreamWriter(bout, "UTF-8");
	}
	
	public void close() throws IOException {
		output.close();
	}
	
	public void write(String text) throws Exception {
		if (output != null) {
			output.write(text);
			//on peut utiliser plusieurs fois methode write
			output.flush();
			//ensuite flush envoie dans le fichier, ne pas oublier cette methode pour le BufferedWriter
		}
		else
			throw new Exception("Writer need to be open first !");
	}
}
