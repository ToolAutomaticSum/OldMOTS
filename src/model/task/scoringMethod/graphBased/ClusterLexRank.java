package model.task.scoringMethod.graphBased;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import model.task.process.AbstractProcess;
import model.task.scoringMethod.Centroid;
import model.task.scoringMethod.ScoreBasedOut;
import textModeling.SentenceModel;
import textModeling.cluster.ClusterCentroid;
import textModeling.graphBased.GraphSentenceBased;
import textModeling.graphBased.NodeGraphSentenceBased;
import textModeling.wordIndex.Dictionnary;
import tools.PairSentenceScore;
import tools.Tools;
import tools.sentenceSimilarity.SentenceSimilarityMetric;
import tools.vector.ToolsVector;

public class ClusterLexRank extends Centroid implements ScoreBasedOut {

	private TreeSet<PairSentenceScore> sentencesScores;
	private double dumpingParameter = 0.85;
	private double epsilon = 0.01;
	
	/** Associé à listCluster issu de Centroid */
	private Map<Integer, GraphSentenceBased> listGraph;
	private double graphThreshold = 0;
	
	public ClusterLexRank(int id) throws Exception {
		super(id);
	}
	
	@Override
	public void init(AbstractProcess currentProcess, Dictionnary dictionnary, Map<Integer, String> hashMapWord)
			throws Exception {
		super.init(currentProcess, dictionnary, hashMapWord);

		graphThreshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "GraphThreshold"));
		
		String similarityMethod = getCurrentProcess().getModel().getProcessOption(id, "SimilarityMethod");
		
		SentenceSimilarityMetric sim = Tools.instanciateSentenceSimilarity(similarityMethod, sentenceCaracteristic);
		
		/** Création de la liste des graphes associés à chaque cluster de listCluster */
		listGraph = new HashMap<Integer, GraphSentenceBased>();
		
		Iterator<ClusterCentroid> itCluster = listCluster.values().iterator();
		while (itCluster .hasNext()) {
			ClusterCentroid cluster = itCluster.next();
			if (cluster.size() > 4) {
				//System.out.println(cluster);
				GraphSentenceBased graph = new GraphSentenceBased(graphThreshold, sim);
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
		sentencesScores = new TreeSet<PairSentenceScore>(); 
		
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
	public TreeSet<PairSentenceScore> getScore() {
		return sentencesScores;
	}
}
