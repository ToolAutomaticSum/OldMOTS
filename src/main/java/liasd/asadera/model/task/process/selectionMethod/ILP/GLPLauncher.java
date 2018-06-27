package main.java.liasd.asadera.model.task.process.selectionMethod.ILP;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GLPLauncher {

	private static Logger logger = LoggerFactory.getLogger(GLPLauncher.class);
	
	private String entryFile;

	public GLPLauncher(String entryFile) {
		this.entryFile = entryFile;
	}

	public void runGLP(String fileName) {
		logger.trace("Starting solver");
		try {
			logger.trace(fileName);
			String[] commande = { "glpsol", "--tmlim", "100", "--lp", entryFile, "-o", fileName};
			Process p = Runtime.getRuntime().exec(commande);

			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.trace("End solver");
	}
}