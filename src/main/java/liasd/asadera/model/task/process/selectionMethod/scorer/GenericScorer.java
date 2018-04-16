package main.java.liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.Map;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.Summary;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class GenericScorer extends Scorer implements SentenceCaracteristicBasedIn {

	private Map<SentenceModel, Object> sentenceCaracteristic;
	private double[] docVector;
	private int dimension;
	private SimilarityMetric sim;

	public GenericScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		listParameterIn.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@Override
	public void init() throws Exception {
		dimension = ((double[]) sentenceCaracteristic.values().iterator().next()).length;
		docVector = new double[dimension];

		for (Object vector : sentenceCaracteristic.values())
			docVector = ToolsVector.somme(docVector, (double[]) vector);

		String similarityMethod = method.getCurrentProcess().getModel().getProcessOption(method.getId(),
				"SimilarityMethod");
		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
	}

	@Override
	public double getScore(Summary summary) throws Exception {
		if (summary.getScore() != 0)
			return summary.getScore();
		double[] sumVector = new double[dimension];
		double score = 0;
		for (SentenceModel sen : summary)
			sumVector = ToolsVector.somme(sumVector, (double[]) sentenceCaracteristic.get(sen));
		score += sim.computeSimilarity(docVector, sumVector);

		summary.setScore(score);
		return score;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> senSim) {
		this.sentenceCaracteristic = senSim;
	}
}
