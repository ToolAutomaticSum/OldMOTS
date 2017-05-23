package model.task.process.indexBuilder.ILP;

import java.io.IOException;

import model.task.postProcess.EvaluationROUGE;

public class GLPLauncher {
	private String entryFile;

	public GLPLauncher(String entryFile) {
		this.entryFile = entryFile;
	}

	public void runGLP() {
		System.out.println("Début du programme");
		try {
			String[] commande = { "glpsol", "--tmlim", "100", "--lp", entryFile, "-o", "sortie_ilp.sol" };
			Process p = Runtime.getRuntime().exec(commande);
			// EvaluationROUGE.inheritIO(p.getInputStream(), System.out);
			EvaluationROUGE.inheritIO(p.getErrorStream(), System.err);
			p.waitFor();
			Thread.sleep(5000);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Fin du programme");
	}
}