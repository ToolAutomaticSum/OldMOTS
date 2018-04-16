package main.java.liasd.asadera.tools.sentenceSimilarity;

import main.java.liasd.asadera.exception.VectorDimensionException;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class CosineSimilarity extends SimilarityMetric {

	public CosineSimilarity() {
		super();
	}

	@Override
	public double computeSimilarity(Object s1, Object s2) throws VectorDimensionException {
		if (s1 instanceof double[]) {
			double[] v1 = (double[]) s1;
			double[] v2 = (double[]) s2;
			double temp = ToolsVector.cosineSimilarity(v1, v2);
			if (temp > 1)
				temp = 1.0;
			return temp;
		} else if (s1 instanceof double[][])
			throw new RuntimeException("Similarity Measure for vector only.");
		else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for vector only.");
		throw new RuntimeException("4D vector can't be compute for now.");
	}
}
