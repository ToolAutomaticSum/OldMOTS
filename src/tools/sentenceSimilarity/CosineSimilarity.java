package tools.sentenceSimilarity;

import java.util.Map;

import exception.VectorDimensionException;
import textModeling.SentenceModel;
import tools.vector.ToolsVector;

public class CosineSimilarity extends SentenceSimilarityMetric {

	public CosineSimilarity(Map<SentenceModel, double[]> sentenceCaracteristic) {
		super(sentenceCaracteristic);
	}

	@Override
	public double computeSimilarity(SentenceModel s1, SentenceModel s2) throws VectorDimensionException {
		return ToolsVector.cosineSimilarity(sentenceCaracteristic.get(s1), sentenceCaracteristic.get(s2));
	}
}
