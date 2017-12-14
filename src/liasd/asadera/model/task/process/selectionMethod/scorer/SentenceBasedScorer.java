package liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.Map;

import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.scoringMethod.ScoreBasedIn;
import liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.Summary;
import liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class SentenceBasedScorer extends Scorer implements ScoreBasedIn, SentenceCaracteristicBasedIn {
	
	private Map<SentenceModel, Double> score;
	private Map<SentenceModel, Object> sentenceCaracteristic;
//	private double[] docVector;
//	private int dimension;
	private SimilarityMetric sim;
	private double lambda;
	
	public SentenceBasedScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		listParameterIn.add(new ParametrizedType(Double.class, Map.class, ScoreBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParametrizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@Override
	public void init() throws Exception {
//		dimension = ((double[])sentenceCaracteristic.values().iterator().next()).length;
//		docVector = new double[dimension];
//		
//		for (Object vector : sentenceCaracteristic.values())
//			docVector = ToolsVector.somme(docVector, (double[]) vector);
		
		String similarityMethod = method.getCurrentProcess().getModel().getProcessOption(method.getId(), "SimilarityMethod");
		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
		lambda = Double.parseDouble(method.getCurrentProcess().getModel().getProcessOption(method.getId(), "Lambda"));
	}

	@Override
	public double getScore(Summary summary) throws Exception {
		if (summary.getScore() != 0)
			return summary.getScore();
//		double[] sumVector = new double[dimension];
		double value = 0;
		for (SentenceModel sen : summary) {
			value += score.get(sen) + 1/sen.getPosition();
//			sumVector = ToolsVector.somme(sumVector, (double[])sentenceCaracteristic.get(sen));
		}
//		value /= summary.size()/2;
//		value = sim.computeSimilarity(docVector, sumVector);
		value *= lambda;
		
		for (int i=0; i<summary.size(); i++)
			for (int j=i+1; j<summary.size(); j++)
				value += (1-lambda)*sim.computeSimilarity(sentenceCaracteristic, summary.get(i), summary.get(j));
		summary.setScore(value);
		return value;
	}

	@Override
	public void setScore(Map<SentenceModel, Double> score) {
		this.score = score;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> senSim) {
		this.sentenceCaracteristic = senSim;
	}
}
