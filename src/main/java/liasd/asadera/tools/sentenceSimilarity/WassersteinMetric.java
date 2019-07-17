package main.java.liasd.asadera.tools.sentenceSimilarity;

import main.java.liasd.asadera.exception.VectorDimensionException;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;

public class WassersteinMetric extends SimilarityMetric {

	EarthMoversDistance distance = new EarthMoversDistance();
	
	@Override
	public double computeSimilarity(Object s1, Object s2) throws Exception {
		if (s1 instanceof double[]) {
			double[] v1 = (double[]) s1;
			double[] v2 = (double[]) s2;
			if (v1.length != v2.length)
				throw new VectorDimensionException();
			return distance.compute(v1, v2);
		} else if (s1 instanceof double[][])
			throw new RuntimeException("Similarity Measure for vector only.");
		else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for vector only.");
		throw new RuntimeException("4D vector can't be compute for now.");
	}

}
