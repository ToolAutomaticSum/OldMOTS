package model.task.process.scoringMethod.graphBased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import exception.LacksOfFeatures;
import model.task.process.AbstractProcess;
import model.task.process.old.VectorCaracteristicBasedIn;
import model.task.process.old.VectorCaracteristicBasedOut;
import model.task.process.scoringMethod.AbstractScoringMethod;
import model.task.process.tempScoringMethod.ScoreBasedOut;
import optimize.SupportADNException;
import textModeling.SentenceModel;
import textModeling.graphBased.GraphSentenceBased;
import textModeling.graphBased.NodeGraphSentenceBased;
import textModeling.wordIndex.Index;
import tools.PairSentenceScore;
import tools.sentenceSimilarity.SentenceSimilarityMetric;
import tools.vector.ToolsVector;

public class LexRank extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut, ScoreBasedOut {

	public static enum LexRank_Parameter {
		DumpingParameter("DumpingParameter"),
		//Epsilon("Epsilon"),
		GraphThreshold("GraphThreshold");

		private String name;

		private LexRank_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	/**
	 * VectorCaracteristicBased
	 */
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	/**
	 * ScoreBasedOut
	 */
	private ArrayList<PairSentenceScore> sentencesScores;
	/**
	 * Dans ADN
	 */
	private double dumpingParameter = 0.85;
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
	
	public LexRank(int id) throws SupportADNException, NumberFormatException, LacksOfFeatures {
		super(id);
		supportADN = new HashMap<String, Class<?>>();
		//supportADN.put("DumpingParameter", Double.class);
		//supportADN.put("Epsilon", Double.class);
		//supportADN.put("GraphThreshold", Double.class);
	}
	
	@Override
	public AbstractScoringMethod makeCopy() throws Exception {
		LexRank p = new LexRank(id);
		initCopy(p);
		return p;
	}
	
	@Override
	public void initADN() throws Exception {
		/*getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.DumpingParameter.getName(), Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.DumpingParameter.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.DumpingParameter.getName()).setMaxValue(0.6);
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.DumpingParameter.getName()).setMinValue(0.0);
		//getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.Epsilon.getName(), 0.0001));
		getCurrentProcess().getADN().putParameter(new Parameter<Double>(LexRank_Parameter.GraphThreshold.getName(), Double.parseDouble(getModel().getProcessOption(id, LexRank_Parameter.GraphThreshold.getName()))));
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName()).setMaxValue(0.6);
		getCurrentProcess().getADN().getParameter(Double.class, LexRank_Parameter.GraphThreshold.getName()).setMinValue(0.0);
	*/}

	@Override
	public void init(AbstractProcess currentProcess, Index dictionnary) throws Exception {
		super.init(currentProcess, dictionnary);
		
		dumpingParameter = 0.15;//getCurrentProcess().getADN().getParameterValue(Double.class, LexRank_Parameter.DumpingParameter.getName());
		epsilon = 0.00001;	///getCurrentProcess().getADN().getParameterValue(Double.class, LexRank_Parameter.Epsilon.getName());
		graphThreshold = 0.1;//getCurrentProcess().getADN().getParameterValue(Double.class, LexRank_Parameter.GraphThreshold.getName());
		
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

			 for (int j=0;j<graph.size();j++) { //phrases courantes
				 for (int k=0;k<graph.size();k++) { //phrases adjencete
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
		 //System.out.println(sentencesScores);
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
			pt[i] = 1.0 / (double)matSize;
		for (int i = 0; i<matSize; i++)
			for (int j = 0; j<matSize; j++)
				matAdj[i][j] = dampingFactor / (double)matSize + (1-dampingFactor)*matAdj[i][j];
		//ToolsVector.transposeMatrix(matAdj);
		//int n = 0;
		do {
			for (int i = 0; i<matSize; i++)
				ptprec[i] = pt[i];
			//Parcours des phrases (phrase courante i)
			for (int i = 0; i<matSize; i++) {
				pt[i] = 0;
				for (int j = 0; j<matSize; j++) { //parcours des phrases adjacentes j
					if (i!=j)
						pt[i] += matAdj[i][j] * ptprec[j];
				}
			}
			//n++;
			normeDiff = ToolsVector.norme(ToolsVector.soustraction(pt, ptprec));
			//System.out.println(normeDiff);
		} while (normeDiff > epsilon);
		//System.out.println("Itération " + n);
		return pt;
	}
}
