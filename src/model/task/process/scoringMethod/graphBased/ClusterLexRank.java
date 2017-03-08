package model.task.process.scoringMethod.graphBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import model.task.process.AbstractProcess;
import model.task.process.scoringMethod.CentroidError;
import model.task.process.scoringMethod.ScoreBasedOut;
import optimize.parameter.Parameter;
import textModeling.SentenceModel;
import textModeling.cluster.ClusterCentroid;
import textModeling.graphBased.GraphSentenceBased;
import textModeling.graphBased.NodeGraphSentenceBased;
import textModeling.wordIndex.Index;
import tools.PairSentenceScore;
import tools.sentenceSimilarity.SentenceSimilarityMetric;
import tools.vector.ToolsVector;

public class ClusterLexRank extends CentroidError implements ScoreBasedOut {

	static {
		supportADN = new HashMap<String, Class<?>>();
		supportADN.put("dumpingParameter", Double.class);
		supportADN.put("epsilon", Double.class);
		supportADN.put("graphThreshold", Double.class);
	}

	public static enum ClusterLexRank_Parameter {
		DumpingParameter("dumpingParameter"),
		Epsilon("epsilon"),
		GraphThreshold("graphThreshold");

		private String name;

		private ClusterLexRank_Parameter(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
	
	private ArrayList<PairSentenceScore> sentencesScores;
	private double dumpingParameter;
	private double epsilon;
	
	/** Associé à listCluster issu de Centroid */
	private Map<Integer, GraphSentenceBased> listGraph;
	private double graphThreshold = 0;
	
	public ClusterLexRank(int id) throws Exception {
		super(id);
		
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.DumpingParameter.getName(), 0.85));
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.Epsilon.getName(), 0.01));
		adn.putParameter(new Parameter<Double>(ClusterLexRank_Parameter.GraphThreshold.getName(), Double.parseDouble(getModel().getProcessOption(id, ClusterLexRank_Parameter.GraphThreshold.getName()))));
	}
	
	@Override
	public void init(AbstractProcess currentProcess, Index dictionnary)
			throws Exception {
		super.init(currentProcess, dictionnary);
		
		dumpingParameter = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.DumpingParameter.getName());
		epsilon = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.Epsilon.getName());
		graphThreshold = adn.getParameterValue(Double.class, ClusterLexRank_Parameter.GraphThreshold.getName());
		
		//graphThreshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "GraphThreshold"));
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		SentenceSimilarityMetric sim = SentenceSimilarityMetric.instanciateSentenceSimilarity(similarityMethod);
		
		/** Création de la liste des graphes associés à chaque cluster de listCluster */
		listGraph = new HashMap<Integer, GraphSentenceBased>();
		
		Iterator<ClusterCentroid> itCluster = listCluster.values().iterator();
		while (itCluster .hasNext()) {
			ClusterCentroid cluster = itCluster.next();
			if (cluster.size() > 1) {
				//System.out.println(cluster);
				GraphSentenceBased graph = new GraphSentenceBased(graphThreshold, sentenceCaracteristic, sim);
				listGraph.put(cluster.getId(), graph);
				
				int sentenceId = 0;
				Iterator<SentenceModel> itSentence = cluster.iterator();
				while (itSentence.hasNext()) {
					graph.add(new NodeGraphSentenceBased(sentenceId, itSentence.next()));
					sentenceId++;
				}
			
				graph.generateGraph();
				//System.out.println(graph);
			} else
				listGraph.put(cluster.getId(), null);
		}
	}

	@Override
	public void computeScores() throws Exception {
		sentencesScores = new ArrayList<PairSentenceScore>(); 
		
		for (int i = 0; i<listCluster.size();i++) {
			GraphSentenceBased graph = listGraph.get(i);
			if (graph != null) {
				double[][] tempMat = new double[graph.size()][graph.size()];
				double[][] matAdj = graph.getMatAdj();
				int[] degree = graph.getDegree();
				for (int j=0;j<graph.size();j++) {
					for (int k=0;k<graph.size();k++) {
						tempMat[j][k] = dumpingParameter/graph.size()+(1-dumpingParameter)*matAdj[j][k]/degree[j];
					}
				}
				double[] result = computeLexRankScore(tempMat, graph.size(), epsilon);
				for (NodeGraphSentenceBased n : graph) {
					sentencesScores.add(new PairSentenceScore(n.getCurrentSentence(), result[n.getIdNode()]));
				}
			}
			else {
				for (SentenceModel s : listCluster.get(i))
					sentencesScores.add(new PairSentenceScore(s, 0.0));
			}
		}
		
		super.computeScores();
	}
	
	public static double[] computeLexRankScore(double[][] matAdj, int matSize, double epsilon) throws Exception {
		double[] pt = new double[matSize];
		double[] ptprec = new double[matSize];
		double[][] tM = ToolsVector.transposeMatrix(matAdj);

		for (int i = 0; i<matSize; i++)
			ptprec[i] = 1.0/matSize;
		do {
			for (int i = 0; i<matSize; i++) {
				for (int j = 0; j<matSize; j++) {
					pt[i] += tM[i][j]*ptprec[j];
				}
			}
		} while (ToolsVector.norme(ToolsVector.soustraction(pt, ptprec)) < epsilon);
		return pt;
	}

	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}
}
