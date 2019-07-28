package main.java.liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.HashSet;
import java.util.Set;

import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public class SumWeigthScorer extends Scorer {

	public SumWeigthScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

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
				score += wi.getWeight();
				listWI.add(wi);
			}

		summary.setScore(score);
		return score;
	}
}
