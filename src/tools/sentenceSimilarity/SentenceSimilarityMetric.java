package tools.sentenceSimilarity;

import java.util.Map;

import textModeling.SentenceModel;

public abstract class SentenceSimilarityMetric {	

	Map<SentenceModel, double[]> sentenceCaracteristic;
	
	public SentenceSimilarityMetric(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
	
	public abstract double computeSimilarity(SentenceModel s1, SentenceModel s2) throws Exception;
}
