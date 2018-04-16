package main.java.liasd.asadera.tools.sentenceSimilarity;

import main.java.liasd.asadera.tools.matrix.ToolsMatrix;

public class FrobeniusDistance extends SimilarityMetric {

	public FrobeniusDistance() {
		super();
	}

	@Override
	public double computeSimilarity(Object s1, Object s2) throws Exception {
		if (s1 instanceof double[])
			throw new RuntimeException("Similarity Measure for matrix only.");
		else if (s1 instanceof double[][]) {
			double[][] m1 = (double[][]) s1;
			double[][] m2 = (double[][]) s2;
			double norme1 = norme(m1);
			double norme2 = norme(m2);
			if (norme1 == 0 || norme2 == 0)
				throw new RuntimeException("Matrix norme is null");
			return innerProduct(m1, m2) / (norme1 * norme2);
		} else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for matrix only.");
		;
		throw new RuntimeException("4D vector can't be compute for now.");
	}

	private double innerProduct(double[][] m1, double[][] m2) {
		return ToolsMatrix.trace(ToolsMatrix.multiply(m2, ToolsMatrix.transpose(m1)));
	}

	private double norme(double[][] a) {
		return Math.sqrt(innerProduct(a, a));
	}
}
