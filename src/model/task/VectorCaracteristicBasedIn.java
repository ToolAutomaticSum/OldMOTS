package model.task;

import java.util.Map;

import textModeling.SentenceModel;

public interface VectorCaracteristicBasedIn {


	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic);
}
