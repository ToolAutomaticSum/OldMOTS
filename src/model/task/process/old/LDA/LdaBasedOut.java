package model.task.process.old.LDA;

import model.task.process.old.VectorCaracteristicBasedOut;

public interface LdaBasedOut extends VectorCaracteristicBasedOut {
	
	public double[] getTheta();
	public int getK();
}
