package main.java.liasd.asadera.tools.sentenceSimilarity;

import main.java.liasd.asadera.exception.VectorDimensionException;

public class MeanWordSimilarity extends SimilarityMetric {

	protected SimilarityMetric sim;

	public MeanWordSimilarity() {
		super();
	}

	@Override
	public double computeSimilarity(Object s1, Object s2) throws Exception {
		if (s1 instanceof double[])
			throw new VectorDimensionException("Can't measure similarity for vector !");
		else if (s1 instanceof double[][]) {
			double[][] m1 = (double[][]) s1;
			double[][] m2 = (double[][]) s2;
			double meanSim = 0;
			for (int i = 0; i < m1.length; i++)
				for (int j = 0; j < m1.length; j++)
					meanSim += sim.computeSimilarity(m1[i], m2[j]);
			return meanSim;
		} else if (s1 instanceof double[][][]) {
			double[][][] m1 = (double[][][]) s1;
			double[][][] m2 = (double[][][]) s2;
			double meanSim = 0;
			for (int i = 0; i < m1.length; i++)
				for (int j = 0; j < m1.length; j++)
					meanSim += sim.computeSimilarity(m1[i], m2[j]);
			return meanSim;
		}
		throw new VectorDimensionException("4D vector (or more) can't be compute for now.");
	}
}
