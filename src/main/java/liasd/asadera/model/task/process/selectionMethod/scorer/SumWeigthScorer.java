package main.java.liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.HashSet;
import java.util.Set;

import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class SumWeigthScorer extends Scorer {

	private SimilarityMetric sim;

	public SumWeigthScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		try {
			sim = SimilarityMetric.instanciateSentenceSimilarity("CosineSimilarity");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() throws Exception {
	}

	@Override
	public double getScore(Summary summary) throws Exception {
		if (summary.getScore() != 0)
			return summary.getScore();

		double score = 0;
		Set<WordIndex> listWI = new HashSet<WordIndex>();
		for (SentenceModel sen : summary)
			for (WordIndex wi : sen) {
//				double similarity = 0;
//				for (WordIndex wi2 : listWI)
//					similarity = Math.max(similarity, sim.computeSimilarity(((WordVector) wi).getWordVector(),
//							((WordVector) wi2).getWordVector()));
//				if (similarity < 0.95)
				score += wi.getWeight();
				listWI.add(wi);
			}

		summary.setScore(score);
		return score;
	}
}
