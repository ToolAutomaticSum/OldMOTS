package liasd.asadera.model.task.process.selectionMethod.scorer;

import java.util.Map;

import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.processCompatibility.ParametrizedType;
import liasd.asadera.model.task.process.selectionMethod.AbstractSelectionMethod;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.Summary;
import liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;
import liasd.asadera.tools.vector.ToolsVector;

public class GenericScorer extends Scorer implements SentenceCaracteristicBasedIn {

	private Map<SentenceModel, Object> sentenceCaracteristic;
	private double[] docVector;
	private int dimension;
//	private double lambda;
	private SimilarityMetric sim;
	

	public GenericScorer(AbstractSelectionMethod method) throws SupportADNException {
		super(method);

		listParameterIn.add(new ParametrizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
	}
	
	@Override
	public void init() throws Exception {
		dimension = ((double[])sentenceCaracteristic.values().iterator().next()).length;
		docVector = new double[dimension];
		
		for (Object vector : sentenceCaracteristic.values())
			docVector = ToolsVector.somme(docVector, (double[]) vector);
//		for (int i=0; i<dimension; i++)
//			docVector[i] /= sentenceCaracteristic.size();

		String similarityMethod = method.getCurrentProcess().getModel().getProcessOption(method.getId(), "SimilarityMethod");
		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
//		lambda = Double.parseDouble(method.getCurrentProcess().getModel().getProcessOption(method.getId(), "Lambda"));
	}
	
	@Override
	public double getScore(Summary summary) throws Exception {
		if (summary.getScore() != 0)
			return summary.getScore();
		double[] sumVector = new double[dimension];
		double score = 0;
		for (SentenceModel sen : summary)
			sumVector = ToolsVector.somme(sumVector, (double[])sentenceCaracteristic.get(sen));
//			score += sim.computeSimilarity(sentenceCaracteristic, docVector, sen) + 1/sen.getPosition();
		
//		score *= lambda;
//		for (int i=0; i<summary.size(); i++)
//			for (int j=i+1; j<summary.size(); j++)
//				score += (1-lambda)*sim.computeSimilarity(sentenceCaracteristic, summary.get(i), summary.get(j));
		score += sim.computeSimilarity(docVector, sumVector);

		summary.setScore(score);
		return score;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> senSim) {
		this.sentenceCaracteristic = senSim;
	}
}
