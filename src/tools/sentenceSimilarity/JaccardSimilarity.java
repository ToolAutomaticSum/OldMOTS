package tools.sentenceSimilarity;

import exception.VectorDimensionException;

public class JaccardSimilarity extends SentenceSimilarityMetric {

	@Override
	public double computeSimilarity(double[] s1, double[] s2) throws Exception {
		if (s1.length != s2.length)
			throw new VectorDimensionException();
		
		int intersection = 0;
		int union = 0;
		for (int i = 0; i < s1.length; i++) {
			if (s1[i] != 0 && s2[i] != 0)
				intersection++;
			if (s1[i] != 0 || s2[i] != 0)
				union++;
		}
		return (double)(intersection)/(double)union;
	}

}
