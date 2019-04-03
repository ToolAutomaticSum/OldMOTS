package main.java.liasd.asadera.model.task.process.scoringMethod.graphBased;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import main.java.liasd.asadera.model.task.process.caracteristicBuilder.SentenceCaracteristicBasedIn;
import main.java.liasd.asadera.model.task.process.indexBuilder.ListSentenceBasedIn;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.model.task.process.scoringMethod.AbstractScoringMethod;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.optimize.parameter.Parameter;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class LexRank extends AbstractScoringMethod implements ListSentenceBasedIn, SentenceCaracteristicBasedIn {

//	private static Logger logger = LoggerFactory.getLogger(LexRank.class);
	
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
	/*
	 * ListSentenceBased
	 */
	private List<SentenceModel> listSen;
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
	private double epsilon = 0.0001;
	/**
	 * Dans ADN
	 */
	private double graphThreshold = 0;
	
	private SimpleWeightedGraph<SentenceModel, DefaultWeightedEdge> graph;	
	
	public LexRank(int id) throws SupportADNException {
		super(id);

		supportADN.put("DampingParameter", Double.class);
		// supportADN.put("Epsilon", Double.class);
		supportADN.put("GraphThreshold", Double.class);

		listParameterIn.add(new ParameterizedType(SentenceModel.class, List.class, ListSentenceBasedIn.class));
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
		epsilon = 0.0001; /// getCurrentProcess().getADN().getParameterValue(Double.class,
						  /// LexRank_Parameter.Epsilon.getName());
		graphThreshold = getCurrentProcess().getADN().getParameterValue(Double.class,
				LexRank_Parameter.GraphThreshold.getName());
		
		/**
		 * Graph construction
		 */
		graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		for(SentenceModel sentence : listSen) {
			graph.addVertex(sentence);
		}

		for(int i=0; i<listSen.size();i++) {
			for(int j=i+1; j<listSen.size();j++) {
				if (i!=j) {
					double weight = sim.computeSimilarity(sentenceCaracteristic, listSen.get(i), listSen.get(j));
					if (weight > graphThreshold) {
						DefaultWeightedEdge edge = new DefaultWeightedEdge();
						graph.addEdge(listSen.get(i), listSen.get(j), edge);
						graph.setEdgeWeight(edge, weight);
					}
				}
			}
		}
	}

	@Override
	public void computeScores(List<Corpus> listCorpus) throws Exception {
		init();
		if (graph != null) {
			
			PageRank<SentenceModel, DefaultWeightedEdge> pr = new PageRank<SentenceModel, DefaultWeightedEdge>(graph, dampingParameter, 100, epsilon);
			sentencesScore.putAll(pr.getScores());
			double max = Collections.max(sentencesScore.values());
			
			for (Entry<SentenceModel, Double> e : sentencesScore.entrySet()) {
				e.setValue(e.getValue() / max);
				e.getKey().setScore(e.getValue());
			}
		}
	}

	@Override
	public void setListSentence(List<SentenceModel> listSen) {
		this.listSen = listSen;
	}

	@Override
	public void setCaracterisics(Map<SentenceModel, Object> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}
}
