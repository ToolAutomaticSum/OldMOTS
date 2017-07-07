package model.task.process.selectionMethod.ILP;

import java.io.IOException;

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
			//EvaluationROUGE.inheritIO(p.getErrorStream(), System.err);
			/*BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
		    while ((line = input.readLine()) != null)
		    	System.out.println(line);*/
			p.waitFor();
			//Thread.sleep(200);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Fin du programme");
	}
}