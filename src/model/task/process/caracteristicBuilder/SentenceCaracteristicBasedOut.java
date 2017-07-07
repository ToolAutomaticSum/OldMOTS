package model.task.process.caracteristicBuilder;

import java.util.Map;

import textModeling.SentenceModel;

public interface SentenceCaracteristicBasedOut {
	public Map<SentenceModel, Object> getVectorCaracterisic();
}
