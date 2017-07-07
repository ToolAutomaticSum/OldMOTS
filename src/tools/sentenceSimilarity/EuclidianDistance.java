package tools.sentenceSimilarity;

import java.util.Map;

import exception.VectorDimensionException;
import model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import model.task.process.processCompatibility.ParametrizedType;

public class EuclidianDistance extends SentenceSimilarityMetric {

	public EuclidianDistance() {
		super();
		listParameterIn.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}
	
	@Override
	protected double computeSimilarity(Object s1, Object s2) throws Exception {
		if (s1 instanceof double[]) {
			double[] v1 = (double[]) s1;
			double[] v2 = (double[]) s2;
			if (v1.length != v2.length)
				throw new VectorDimensionException();
			return euclidianDistance(v1, v2);
		}
		else if (s1 instanceof double[][])
			throw new RuntimeException("Similarity Measure for vector only.");
		else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for vector only.");
		throw new RuntimeException("4D vector can't be compute for now.");
		
	}
	
	public static double euclidianDistance(double[] a, double[] b) {
		double sum = 0;
		for (int i = 0; i<a.length; i++)
			sum += Math.pow(a[i]-b[i], 2);
		return Math.sqrt(sum);
	}

}
