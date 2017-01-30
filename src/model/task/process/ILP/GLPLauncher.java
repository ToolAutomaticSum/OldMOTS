package model.task.process.ILP;

import java.io.IOException;

import tools.StreamPrinter;


public class GLPLauncher
{
	private String entryFile;
	
	public GLPLauncher(String entryFile) {
		this.entryFile = entryFile;
	}
	public void runGLP() {
        System.out.println("Début du programme");
        try {
            String[] commande = {"G:\\Thèse\\Solveur\\glpk-4.60\\w64\\glpsol.exe", "--tmlim", "100", "--lp", entryFile, "-o", "sortie_ilp.sol"};
            Process p = Runtime.getRuntime().exec(commande);
            StreamPrinter fluxSortie = new StreamPrinter(p.getInputStream());
            StreamPrinter fluxErreur = new StreamPrinter(p.getErrorStream());

            new Thread(fluxSortie).start();
            new Thread(fluxErreur).start();

            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Fin du programme");
	}
}