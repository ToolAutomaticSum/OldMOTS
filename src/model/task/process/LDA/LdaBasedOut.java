package model.task.process.LDA;

import model.task.VectorCaracteristicBasedOut;

public interface LdaBasedOut extends VectorCaracteristicBasedOut {
	
	public double[][] getTheta();
	public int getK();
	public int getNbSentence();
}
