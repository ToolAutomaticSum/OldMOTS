package model.task.process.caracteristicBuilder;

import java.util.Map;

import textModeling.SentenceModel;

public interface SentenceCaracteristicBasedIn<T> {
	public void setCaracterisics(Map<SentenceModel, T> sentenceCaracteristic);
}
