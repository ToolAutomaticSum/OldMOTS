package tools.sentenceSimilarity;

import java.lang.reflect.Constructor;
import java.util.Map;

import textModeling.SentenceModel;

public abstract class SentenceSimilarityMetric {	

	Map<SentenceModel, double[]> sentenceCaracteristic;
	
	public SentenceSimilarityMetric() {
	}
	
	public abstract double computeSimilarity(double[] s1, double[] s2) throws Exception;
	
	public static SentenceSimilarityMetric instanciateSentenceSimilarity(String similarityMethod) throws Exception {
		Class<?> cl;
		cl = Class.forName("tools.sentenceSimilarity." + similarityMethod);
	    //Class types = new Class{Integer.class};
	    @SuppressWarnings("rawtypes")
		Constructor ct = cl.getConstructor();
	    Object o = ct.newInstance();
	    return (SentenceSimilarityMetric) o;
	}
}
