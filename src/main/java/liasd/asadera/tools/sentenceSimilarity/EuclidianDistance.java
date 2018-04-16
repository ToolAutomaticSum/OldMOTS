package main.java.liasd.asadera.tools.sentenceSimilarity;

import main.java.liasd.asadera.exception.VectorDimensionException;

public class EuclidianDistance extends SimilarityMetric {

	public EuclidianDistance() {
		super();
	}

	@Override
	public double computeSimilarity(Object s1, Object s2) throws Exception {
		if (s1 instanceof double[]) {
			double[] v1 = (double[]) s1;
			double[] v2 = (double[]) s2;
			if (v1.length != v2.length)
				throw new VectorDimensionException();
			return euclidianDistance(v1, v2);
		} else if (s1 instanceof double[][])
			throw new RuntimeException("Similarity Measure for vector only.");
		else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for vector only.");
		throw new RuntimeException("4D vector can't be compute for now.");

	}

	public static double euclidianDistance(double[] a, double[] b) {
		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += Math.pow(a[i] - b[i], 2);
		return Math.sqrt(sum);
	}

}
