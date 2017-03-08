package textModeling.graphBased;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import textModeling.SentenceModel;
import tools.sentenceSimilarity.SentenceSimilarityMetric;

public class GraphSentenceBased extends ArrayList<NodeGraphSentenceBased> {

	private SentenceSimilarityMetric sim;
	private Map<SentenceModel, double[]> sentenceCaracteristic;
	private double[][] matAdj;
	private int[] degree;
	private double threshold = 0;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6528014500614693135L;

	public GraphSentenceBased(double threshold, Map<SentenceModel, double[]> sentenceCaracteristic, SentenceSimilarityMetric sim) {
		super();
		this.threshold = threshold;
		this.sentenceCaracteristic = sentenceCaracteristic;
		this.sim = sim;
	}

	public void generateGraph() throws Exception {
		matAdj = new double[this.size()][this.size()];
		degree = new int[this.size()];
		
		for (int i = 0; i<this.size(); i++) {
			for (int j = 0; j<this.size(); j++) {
				matAdj[i][j] = sim.computeSimilarity(sentenceCaracteristic.get(this.get(i).getCurrentSentence()), sentenceCaracteristic.get(this.get(j).getCurrentSentence()));
				if (matAdj[i][j] > threshold) {
					this.get(i).addAdjacentSentence(this.get(j).getCurrentSentence(), matAdj[i][j]);
					degree[i]++;
				} else
					matAdj[i][j] = 0;
			}
		}
	}

	public double[][] getMatAdj() {
		return matAdj;
	}

	public void setMatAdj(double[][] matAdj) {
		this.matAdj = matAdj;
	}

	public int[] getDegree() {
		return degree;
	}

	public void setDegree(int[] degree) {
		this.degree = degree;
	}
	
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.####");
		String str = this.size() + "\n";
		for (int i = 0; i<this.size();i++) {
			for (int j = 0; j<this.size(); j++)
				str += df.format(matAdj[i][j]) + "\t";
			str += "\n";
		}
		return str;
	}
}
