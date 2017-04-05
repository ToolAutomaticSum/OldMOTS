package tools.sentenceSimilarity;

import exception.VectorDimensionException;
import tools.vector.ToolsVector;

public class CosineSimilarity extends SentenceSimilarityMetric {

	public CosineSimilarity() {
		super();
	}

	@Override
	public double computeSimilarity(double[] s1, double[] s2) throws VectorDimensionException {
		double temp = ToolsVector.cosineSimilarity(s1, s2);
		if (temp > 1)
			temp = 1.0;
		return temp;
	}
}
