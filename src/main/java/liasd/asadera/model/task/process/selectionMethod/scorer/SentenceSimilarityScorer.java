package main.java.liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.Map;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class SentenceSimilarityScorer extends Scorer implements SentenceCaracteristicBasedIn {

	private Map<SentenceModel, Object> sentenceCaracteristic;
	private SimilarityMetric sim;

	public SentenceSimilarityScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		listParameterIn.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@Override
	public void init() throws Exception {
		String similarityMethod = method.getCurrentProcess().getModel().getProcessOption(method.getId(),
				"SimilarityMethod");
		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
	}

	@Override
	public double getScore(Summary summary) throws Exception {
		if (summary.getScore() != 0)
			return summary.getScore();
		double score = 0;
		int nbSim = 0;
		for (int i=0; i<summary.size(); i++) {
			for (int j=i+1; j<summary.size(); j++) {
				score += sim.computeSimilarity(sentenceCaracteristic.get(summary.get(i)), summary.get(j));
				nbSim += 1;
			}
		}
		score /= nbSim;

		summary.setScore(score);
		return score;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> senSim) {
		this.sentenceCaracteristic = senSim;
	}
}
