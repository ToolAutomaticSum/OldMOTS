package tools.sentenceSimilarity;

import exception.VectorDimensionException;
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
