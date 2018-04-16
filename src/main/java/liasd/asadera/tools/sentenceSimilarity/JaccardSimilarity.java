package main.java.liasd.asadera.tools.sentenceSimilarity;

import java.util.ArrayList;
import java.util.List;

import main.java.liasd.asadera.exception.VectorDimensionException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.WordModel;

public class JaccardSimilarity extends SimilarityMetric {

	public JaccardSimilarity() {
		super();
	}

	@Override
	public double computeSimilarity(Object s1, Object s2) throws Exception {
		if (s1 instanceof double[]) {
			double[] v1 = (double[]) s1;
			double[] v2 = (double[]) s2;
			if (v1.length != v2.length)
				throw new VectorDimensionException();
			int intersection = 0;
			int union = 0;
			for (int i = 0; i < v1.length; i++) {
				if (v1[i] != 0 && v2[i] != 0)
					intersection++;
				if (v1[i] != 0 || v2[i] != 0)
					union++;
			}
			return (double) (intersection) / (double) union;
		} else if (s1 instanceof double[][])
			throw new RuntimeException("Similarity Measure for vector only.");
		else if (s1 instanceof double[][][])
			throw new RuntimeException("Similarity Measure for vector only.");
		throw new RuntimeException("4D vector can't be compute for now.");
	}

	public double computeSimilarity(SentenceModel s1, SentenceModel s2) {
		List<String> intersection = new ArrayList<String>();
		List<String> union = new ArrayList<String>();
		for (WordModel w : s1.getListWordModel()) {
			if (!intersection.contains(w.getmLemma()) && s2.getListWordModel().contains(w))
				intersection.add(w.getmLemma());
			if (!union.contains(w.getmLemma()))
				union.add(w.getmLemma());
		}
		for (WordModel w : s2.getListWordModel()) {
			if (!intersection.contains(w.getmLemma()) && s1.getListWordModel().contains(w))
				intersection.add(w.getmLemma());
			if (!union.contains(w.getmLemma()))
				union.add(w.getmLemma());
		}
		return (double) (intersection.size()) / (double) (union.size());
	}
}
