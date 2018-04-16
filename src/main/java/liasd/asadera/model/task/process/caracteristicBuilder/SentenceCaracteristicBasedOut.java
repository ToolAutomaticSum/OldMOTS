package main.java.liasd.asadera.model.task.process.caracteristicBuilder;

import java.util.Map;

import main.java.liasd.asadera.textModeling.SentenceModel;

public interface SentenceCaracteristicBasedOut {
	public Map<SentenceModel, Object> getVectorCaracterisic();
}
