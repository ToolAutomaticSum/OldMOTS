package tools.sentenceSimilarity;

import java.util.Map;

import exception.VectorDimensionException;
import textModeling.SentenceModel;
import tools.vector.ToolsVector;

public class CosineSimilarity extends SentenceSimilarityMetric {

	public CosineSimilarity() {
		super();
	}

	@Override
	public double computeSimilarity(double[] s1, double[] s2) throws VectorDimensionException {
		return ToolsVector.cosineSimilarity(s1, s2);
	}
}
