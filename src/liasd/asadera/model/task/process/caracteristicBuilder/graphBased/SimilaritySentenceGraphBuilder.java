package liasd.asadera.model.task.process.caracteristicBuilder.graphBased;

import java.util.List;
import java.util.Map;

import liasd.asadera.model.task.process.caracteristicBuilder.AbstractCaracteristicBuilder;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceGraphBasedIn;
import liasd.asadera.model.task.process.caracteristicBuilder.SentenceGraphBasedOut;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import liasd.asadera.model.task.process.scoringMethod.graphBased.LexRank.LexRank_Parameter;
import liasd.asadera.optimize.SupportADNException;
import liasd.asadera.optimize.parameter.Parameter;
import liasd.asadera.textModeling.Corpus;
import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.graphBased.GraphSentenceBased;
import liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class SimilaritySentenceGraphBuilder extends AbstractCaracteristicBuilder
		implements SentenceCaracteristicBasedIn, SentenceGraphBasedOut {

	private SimilarityMetric sim;
	private double graphThreshold = 0;
	private GraphSentenceBased graph;
	private Map<SentenceModel, Object> sentenceCaracteristic;

	public SimilaritySentenceGraphBuilder(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterOut.add(new ParameterizedType(null, GraphSentenceBased.class, SentenceGraphBasedOut.class));
	}

	@Override
	public AbstractCaracteristicBuilder makeCopy() throws Exception {
		SimilaritySentenceGraphBuilder p = new SimilaritySentenceGraphBuilder(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.GraphThreshold.getName(),
				Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.GraphThreshold.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName())
				.setMaxValue(0.6);
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName())
				.setMinValue(0.0);

		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

		sim = SimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
	}

	@Override
	public void processCaracteristics(List<Corpus> listCorpus) throws Exception {
		graphThreshold = getCurrentProcess().getADN().getParameterValue(Double.class,
				LexRank_Parameter.GraphThreshold.getName());

		graph = new GraphSentenceBased(graphThreshold, sentenceCaracteristic, sim);
		graph.generateGraph();
	}

	@Override
	public void finish() {
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(null, GraphSentenceBased.class, SentenceGraphBasedIn.class));
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((SentenceGraphBasedIn) compMethod).setGraph(getGraph());
		;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> senSim) {
		sentenceCaracteristic = senSim;
	}

	@Override
	public GraphSentenceBased getGraph() {
		return graph;
	}

}
