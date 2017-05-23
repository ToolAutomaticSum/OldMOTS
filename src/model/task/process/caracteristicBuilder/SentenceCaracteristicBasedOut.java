package model.task.process.caracteristicBuilder;

import java.util.Map;

import textModeling.SentenceModel;

public interface SentenceCaracteristicBasedOut<T> {
	public Map<SentenceModel, T> getVectorCaracterisic();
}
