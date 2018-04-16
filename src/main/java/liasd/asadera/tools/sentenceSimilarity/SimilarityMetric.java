package main.java.liasd.asadera.tools.sentenceSimilarity;

import java.lang.reflect.Constructor;
import java.util.Map;

import main.java.liasd.asadera.exception.VectorDimensionException;
import main.java.liasd.asadera.textModeling.SentenceModel;

public abstract class SimilarityMetric {

	public SimilarityMetric() {
	}

	public double computeSimilarity(Map<SentenceModel, Object> sentenceCaracteristic, SentenceModel s1,
			SentenceModel s2) throws Exception {
		return computeSimilarity(sentenceCaracteristic.get(s1), sentenceCaracteristic.get(s2));
	}

	public double computeSimilarity(Map<SentenceModel, Object> sentenceCaracteristic, Object s1, SentenceModel s2)
			throws Exception {
		if (s1.getClass() == sentenceCaracteristic.get(s2).getClass())
			return computeSimilarity(s1, sentenceCaracteristic.get(s2));
		else
			throw new VectorDimensionException("Type error when computing sentence similarity !");
	}

	public abstract double computeSimilarity(Object s1, Object s2) throws Exception;

	public static SimilarityMetric instanciateSentenceSimilarity(String similarityMethod) throws Exception {
		Class<?> cl;
		cl = Class.forName("main.java.liasd.asadera.tools.sentenceSimilarity." + similarityMethod);
		@SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor();
		SimilarityMetric o = (SimilarityMetric) ct.newInstance();
		return (SimilarityMetric) o;
	}
}
