package tools.sentenceSimilarity;

import java.util.Map;

import exception.VectorDimensionException;
import model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import model.task.process.processCompatibility.ParametrizedType;
import tools.vector.ToolsVector;

public class CosineSimilarity extends SentenceSimilarityMetric {
	
	public CosineSimilarity() {
		super();
		listParameterIn.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}
	
	@Override
	protected double computeSimilarity(Object s1, Object s2) throws VectorDimensionException {
		if (s1 instanceof double[]) {
			double[] v1 = (double[]) s1;
			double[] v2 = (double[]) s2;
			double temp = ToolsVector.cosineSimilarity(v1, v2);
			if (temp > 1)
				temp = 1.0;
			return temp;
		}
		else if (s1 instanceof double[][])
			throw new RuntimeException("Similarity Measure for vector only.");
		else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for vector only.");
		throw new RuntimeException("4D vector can't be compute for now.");
	}
}
