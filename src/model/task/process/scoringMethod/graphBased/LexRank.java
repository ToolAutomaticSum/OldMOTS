package model.task.process.scoringMethod.graphBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import exception.LacksOfFeatures;
import model.task.process.AbstractProcess;
import model.task.process.VectorCaracteristicBasedIn;
import model.task.process.VectorCaracteristicBasedOut;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.scoringMethod.ScoreBasedOut;
import model.task.process.scoringMethod.graphBased.ClusterLexRank.ClusterLexRank_Parameter;
import optimize.SupportADNException;
import optimize.parameter.Parameter;
import textModeling.SentenceModel;
import textModeling.graphBased.GraphSentenceBased;
import textModeling.graphBased.NodeGraphSentenceBased;
import textModeling.wordIndex.Index;
import tools.PairSentenceScore;
import tools.sentenceSimilarity.SentenceSimilarityMetric;

public class LexRank extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {

	static {
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("dumpingParameter", Double.class);
		supportADN.put("epsilon", Double.class);
		supportADN.put("graphThreshold", Double.class);
	}

	public static enum LexRank_Parameter {
		DumpingParameter("dumpingParameter"),
		Epsilon("epsilon"),
		GraphThreshold("graphThreshold");

		private String name;

		private LexRank_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	
	private ArrayList<PairSentenceScore> sentencesScores;
	private double dumpingParameter = 0.85;
	private double epsilon = 0.01;
	
	private GraphSentenceBased graph;
	private double graphThreshold = 0;
	
	public LexRank(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);

		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.DumpingParameter.getName(), 0.85));
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.Epsilon.getName(), 0.01));
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.GraphThreshold.getName(), Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.GraphThreshold.getName()))));
	}

	@Override
	public void init(AbstractProcess currentProcess, Index dictionnary) throws Exception {
		super.init(currentProcess, dictionnary);
		
		dumpingParameter = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.DumpingParameter.getName());
		epsilon = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.Epsilon.getName());
		graphThreshold = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.GraphThreshold.getName());
		
		//graphThreshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "GraphThreshold"));
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		SentenceSimilarityMetric sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(similarityMethod);

		graph = new GraphSentenceBased(graphThreshold, sentenceCaracteristic, sim);
		
		int sentenceId = 0;
		Iterator<SentenceModel> itSentence = sentenceCaracteristic.keySet().iterator();
		while (itSentence.hasNext()) {
			graph.add(new NodeGraphSentenceBased(sentenceId, itSentence.next()));
			sentenceId++;
		}
	
		graph.generateGraph();
		System.out.println(graph);
	}
	
	@Override
	public void computeScores() throws Exception {
		sentencesScores = new ArrayList<PairSentenceScore>(); 
		
		 if (graph != null) {
			 double[][] tempMat = new double[graph.size()][graph.size()];
			 double[][] matAdj = graph.getMatAdj();
			 int[] degree = graph.getDegree();
			 for (int j=0;j<graph.size();j++) {
				 for (int k=0;k<graph.size();k++) {
					 tempMat[j][k] = dumpingParameter/graph.size()+(1-dumpingParameter)*matAdj[j][k]/degree[j];
				 }
			 }
			 double[] result = ClusterLexRank.computeLexRankScore(tempMat, graph.size(), epsilon);
			 for (NodeGraphSentenceBased n : graph) {
				 sentencesScores.add(new PairSentenceScore(n.getCurrentSentence(), result[n.getIdNode()]));
			 }
		 }
	}

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;		
	}

	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

}
