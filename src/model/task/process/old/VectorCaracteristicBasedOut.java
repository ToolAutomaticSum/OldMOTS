package model.task.process.old;

import java.util.Map;

import textModeling.SentenceModel;

public interface VectorCaracteristicBasedOut {

	public Map<SentenceModel, double[]> getVectorCaracterisic();
}
