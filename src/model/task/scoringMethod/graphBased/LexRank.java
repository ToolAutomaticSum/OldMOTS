package model.task.scoringMethod.graphBased;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import model.task.VectorCaracteristicBasedIn;
import model.task.VectorCaracteristicBasedOut;
import model.task.process.AbstractProcess;
import model.task.scoringMethod.AbstractScoringMethod;
import model.task.scoringMethod.ScoreBasedOut;
import textModeling.SentenceModel;
import textModeling.graphBased.GraphSentenceBased;
import textModeling.graphBased.NodeGraphSentenceBased;
import textModeling.wordIndex.Dictionnary;
import tools.PairSentenceScore;
import tools.Tools;
import tools.sentenceSimilarity.SentenceSimilarityMetric;

public class LexRank extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {

	private Map<SentenceModel, double[]> sentenceCaracteristic;
	
	private TreeSet<PairSentenceScore> sentencesScores;
	private double dumpingParameter = 0.85;
	private double epsilon = 0.01;
	
	private GraphSentenceBased graph;
	private double graphThreshold = 0;
	
	public LexRank(int id) {
		super(id);
	}

	@Override
	public void init(AbstractProcess currentProcess, Dictionnary dictionnary, Map<Integer, String> hashMapWord) throws Exception {
		super.init(currentProcess, dictionnary, hashMapWord);
		
		graphThreshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "GraphThreshold"));
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		SentenceSimilarityMetric sim = Tools.instanciateSentenceSimilarity(similarityMethod, sentenceCaracteristic);

		graph = new GraphSentenceBased(graphThreshold, sim);
		
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
		sentencesScores = new TreeSet<PairSentenceScore>(); 
		
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
	public TreeSet<PairSentenceScore> getScore() {
		return sentencesScores;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}

}
