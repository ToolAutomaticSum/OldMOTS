package main.java.liasd.asadera.tools.sentenceSimilarity;

import main.java.liasd.asadera.exception.VectorDimensionException;

public class JensenShannonSimilarity extends SimilarityMetric {

	public static final double log2 = Math.log(2);

	public JensenShannonSimilarity() {
		super();
	}

	@Override
	public double computeSimilarity(Object s1, Object s2) throws Exception {
		if (s1 instanceof double[]) {
			double[] v1 = (double[]) s1;
			double[] v2 = (double[]) s2;
			if (v1.length != v2.length)
				throw new VectorDimensionException();
			return jensenShannonDivergence(v1, v2);
		} else if (s1 instanceof double[][])
			throw new RuntimeException("Similarity Measure for vector only.");
		else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for vector only.");
		throw new RuntimeException("4D vector can't be compute for now.");
	}

	public static double jensenShannonDivergence(double[] p1, double[] p2) {
		double[] average = new double[p1.length];
		for (int i = 0; i < p1.length; ++i) {
			average[i] += (p1[i] + p2[i]) / 2;
		}
		return 1 - (klDivergence(p1, average) + klDivergence(p2, average)) / 2;
	}

	/**
	 * Returns the KL divergence, K(p1 || p2).
	 *
	 * The log is w.r.t. base 2.
	 * <p>
	 *
	 * *Note*: If any value in <tt>p2</tt> is <tt>0.0</tt> then the KL-divergence is
	 * <tt>infinite</tt>. Limin changes it to zero instead of infinite.
	 * 
	 */
	public static double klDivergence(double[] p1, double[] p2) {
		double klDiv = 0.0;

		for (int i = 0; i < p1.length; ++i) {
			if (p1[i] == 0) {
				continue;
			}
			if (p2[i] == 0.0) {
				continue;
			} // Limin
			klDiv += p1[i] * Math.log(p1[i] / p2[i]);
		}

		return klDiv / log2;
	}
}
