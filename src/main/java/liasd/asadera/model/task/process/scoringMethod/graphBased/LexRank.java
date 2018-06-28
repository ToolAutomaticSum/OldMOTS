package main.java.liasd.asadera.model.task.process.scoringMethod.graphBased;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.AbstractScoringMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.optimize.parameter.Parameter;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.graphBased.GraphSentenceBased;
import main.java.liasd.asadera.textModeling.graphBased.NodeGraphSentenceBased;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;
import main.java.liasd.asadera.tools.vector.ToolsVector;

public class LexRank extends AbstractScoringMethod implements SentenceCaracteristicBasedIn {

	public static enum LexRank_Parameter {
		DampingParameter("DampingParameter"),
		// Epsilon("Epsilon"),
		GraphThreshold("GraphThreshold");

		private String name;

		private LexRank_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private SimilarityMetric sim;
	/**
	 * SentenceCaracteristicBased
	 */
	private Map<SentenceModel, Object> sentenceCaracteristic;
	/**
	 * Dans ADN
	 */
	private double dampingParameter = 0.85;
	/**
	 * Constant
	 */
	private double epsilon = 0.00001;
	/**
	 * Dans ADN
	 */
	private double graphThreshold = 0;
	/**
	 * Construit dans init
	 */
	private GraphSentenceBased graph;

	public LexRank(int id) throws SupportADNException {
		super(id);

		supportADN.put("DampingParameter", Double.class);
		// supportADN.put("Epsilon", Double.class);
		supportADN.put("GraphThreshold", Double.class);

		listParameterIn.add(new ParameterizedType(double[].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[][].class, Map.class, SentenceCaracteristicBasedIn.class));
		listParameterIn.add(new ParameterizedType(double[][][].class, Map.class, SentenceCaracteristicBasedIn.class));
	}

	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		LexRank p = new LexRank(id);
		initCopy(p);
		return p;
	}

	@Override
	public void initADN() throws Exception {
		super.initADN();
		
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.DampingParameter.getName(),
				Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.DampingParameter.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.DampingParameter.getName())
				.setMaxValue(0.6);
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.DampingParameter.getName())
				.setMinValue(0.0);
		// getCurrentProcess().getADN().putParameter(new
		// Parameter<Double>(LexRank_Parameter.Epsilon.getName(), 0.0001));
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.GraphThreshold.getName(),
				Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.GraphThreshold.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName())
				.setMaxValue(0.6);
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName())
				.setMinValue(0.0);

		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");

		sim = SimilarityMetric.instanciateSentenceSimilarity(/* this, */ similarityMethod);
	}

	private void init() throws Exception {

		dampingParameter = getCurrentProcess().getADN().getParameterValue(Double.class,
				LexRank_Parameter.DampingParameter.getName());
		epsilon = 0.00001; /// getCurrentProcess().getADN().getParameterValue(Double.class,
							/// LexRank_Parameter.Epsilon.getName());
		graphThreshold = getCurrentProcess().getADN().getParameterValue(Double.class,
				LexRank_Parameter.GraphThreshold.getName());

		graph = new GraphSentenceBased(graphThreshold, sentenceCaracteristic, sim);

		graph.generateGraph();
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		init();
		if (graph != null) {
			double[][] tempMat = new double[graph.size()][graph.size()];
			double[][] matAdj = graph.getMatAdj();
			int[] degree = graph.getDegree();

			for (int j = 0; j < graph.size(); j++) {
				for (int k = 0; k < graph.size(); k++) {
					tempMat[j][k] = matAdj[j][k] / degree[k];
				}
			}
			double[] result = LexRank.computeLexRankScore(dampingParameter, tempMat, graph.size(), epsilon);

			double max = 0.0;
			for (NodeGraphSentenceBased n : graph) {
				if (result[n.getIdNode()] > max)
					max = result[n.getIdNode()];
				sentencesScore.put(n.getCurrentSentence(), result[n.getIdNode()]);
			}
			for (Entry<SentenceModel, Double> e : sentencesScore.entrySet()) {
				e.setValue(e.getValue() / max);
				e.getKey().setScore(e.getValue());
			}
		}
	}

	public static double[] computeLexRankScore(double dampingFactor, double[][] matAdj, int matSize, double epsilon)
			throws Exception {
		double[] ptprec = new double[matSize];
		double[] pt = new double[matSize];
		double normeDiff;
		for (int i = 0; i < matSize; i++)
			pt[i] = 1.0 / (double) matSize;
		for (int i = 0; i < matSize; i++)
			for (int j = 0; j < matSize; j++)
				matAdj[i][j] = dampingFactor / (double) matSize + (1 - dampingFactor) * matAdj[i][j];
		do {
			for (int i = 0; i < matSize; i++)
				ptprec[i] = pt[i];
			for (int i = 0; i < matSize; i++) {
				pt[i] = 0;
				for (int j = 0; j < matSize; j++) { 
					if (i != j)
						pt[i] += matAdj[i][j] * ptprec[j];
				}
			}
			normeDiff = ToolsVector.norme(ToolsVector.soustraction(pt, ptprec));
		} while (normeDiff > epsilon);
		return pt;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
}
