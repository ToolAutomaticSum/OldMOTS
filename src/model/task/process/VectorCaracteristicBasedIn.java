package model.task.process;

import java.util.Map;

import textModeling.SentenceModel;

public interface VectorCaracteristicBasedIn {


	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic);
}
