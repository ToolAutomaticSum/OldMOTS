package main.java.liasd.asadera.model.task.process.selectionMethod.ILP;

import java.io.File;
import java.io.IOException;

public class GLPLauncher {
	private String entryFile;

	public GLPLauncher(String entryFile) {
		this.entryFile = entryFile;
	}

	public void runGLP(String fileName) {
		System.out.println("Starting solver");
		try {
			System.out.println(fileName);
			String[] commande = { "glpsol", "--tmlim", "100", "--lp", entryFile, "-o", fileName};
			Process p = Runtime.getRuntime().exec(commande);

			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Fin du programme");
	}
}