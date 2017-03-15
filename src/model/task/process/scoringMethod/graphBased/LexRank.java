package model.task.process.scoringMethod.graphBased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import tools.vector.ToolsVector;

public class LexRank extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {

	static {
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("dumpingParameter", Double.class);
		supportADN.put("epsilon", Double.class);
		supportADN.put("graphThreshold", Double.class);
	}

	public static enum LexRank_Parameter {
		DumpingParameter("DumpingParameter"),
		Epsilon("Epsilon"),
		GraphThreshold("GraphThreshold");

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
	private double epsilon = 0.00001;
	
	private GraphSentenceBased graph;
	private double graphThreshold = 0;
	
	public LexRank(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);
		}

	@Override
	public void init(AbstractProcess currentProcess, Index dictionnary) throws Exception {
		super.init(currentProcess, dictionnary);
		
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.DumpingParameter.getName(), 0.15));
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.Epsilon.getName(), 0.000001));
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.GraphThreshold.getName(), Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.GraphThreshold.getName()))));
		
		dumpingParameter = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.DumpingParameter.getName());
		epsilon = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.Epsilon.getName());
		graphThreshold = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.GraphThreshold.getName());
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		SentenceSimilarityMetric sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(similarityMethod);

		graph = new GraphSentenceBased(graphThreshold, sentenceCaracteristic, sim);
	
		graph.generateGraph();
		//System.out.println(graph);
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
					 tempMat[j][k] = matAdj[j][k]/degree[k];
				 }
			 }
			 double[] result = LexRank.computeLexRankScore(dumpingParameter, tempMat, graph.size(), epsilon);

			 double max = 0.0;
			 for (NodeGraphSentenceBased n : graph) {
				 if (result[n.getIdNode()] > max)
					 max = result[n.getIdNode()];
				 sentencesScores.add(new PairSentenceScore(n.getCurrentSentence(), result[n.getIdNode()]));
			 }
			 for (PairSentenceScore p : sentencesScores)
				 p.setScore(p.getScore()/max);
		 }
		 
		 Collections.sort(sentencesScores);
		 System.out.println(sentencesScores);
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

	public static double[] computeLexRankScore(double dampingFactor, double[][] matAdj, int matSize, double epsilon) throws Exception {
		double[] ptprec = new double[matSize];
		double[] pt = new double[matSize];
		double normeDiff;
		for (int i = 0; i<matSize; i++)
			pt[i] = 1.0 / matSize;
		for (int i = 0; i<matSize; i++)
			for (int j = 0; j<matSize; j++)
				matAdj[i][j] = dampingFactor / matSize + (1-dampingFactor)*matAdj[i][j];
		ToolsVector.transposeMatrix(matAdj);
		int n = 0;
		do {
			for (int i = 0; i<matSize; i++)
				ptprec[i] = pt[i];
			//Parcours des phrases (phrase courante i)
			for (int i = 0; i<matSize; i++) {
				pt[i] = 0;
				for (int j = 0; j<matSize; j++) //parcours des phrases adjacentes j
					pt[i] += matAdj[i][j] * ptprec[j];
			}
			n++;
			System.out.println("Itération " + n);
			normeDiff = ToolsVector.norme(ToolsVector.soustraction(pt, ptprec));
			System.out.println(normeDiff);
		} while (normeDiff > epsilon);
		return pt;
	}
}
