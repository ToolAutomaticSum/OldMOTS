package tools.sentenceSimilarity;

import exception.VectorDimensionException;

public class EuclidianDistance extends SentenceSimilarityMetric {

	@Override
	public double computeSimilarity(double[] a, double[] b) throws Exception {
		if (a.length != b.length)
			throw new VectorDimensionException();
		return euclidianDistance(a, b);
	}
	
	public static double euclidianDistance(double[] a, double[] b) {
		double sum = 0;
		for (int i = 0; i<a.length; i++)
			sum += Math.pow(a[i]-b[i], 2);
		return Math.sqrt(sum);
	}

}
