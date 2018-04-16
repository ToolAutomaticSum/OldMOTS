package main.java.liasd.asadera.textModeling.graphBased;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.tools.sentenceSimilarity.SimilarityMetric;

public class GraphSentenceBased extends ArrayList<NodeGraphSentenceBased> {

	private SimilarityMetric sim;
	private Map<SentenceModel, Object> sentenceCaracteristic;
	private double[][] matAdj;
	private int[] degree;
	private double threshold = 0;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6528014500614693135L;

	public GraphSentenceBased(double threshold, Map<SentenceModel, Object> sentenceCaracteristic,
			SimilarityMetric sim) {
		super();
		this.threshold = threshold;
		this.sentenceCaracteristic = sentenceCaracteristic;
		this.sim = sim;

		int sentenceId = 0;
		Iterator<SentenceModel> itSentence = sentenceCaracteristic.keySet().iterator();
		while (itSentence.hasNext()) {
			this.add(new NodeGraphSentenceBased(sentenceId, itSentence.next()));
			sentenceId++;
		}
	}

	public void generateGraph() throws Exception {
		matAdj = new double[this.size()][this.size()];
		degree = new int[this.size()];
		for (int i = 0; i < this.size(); i++) {
			for (int j = 0; j < this.size(); j++) {
				if (i == j)
					matAdj[i][j] = 1.0;
				else
					matAdj[i][j] = sim.computeSimilarity(sentenceCaracteristic, this.get(i).getCurrentSentence(),
							this.get(j).getCurrentSentence());
				if (matAdj[i][j] > threshold) {
					this.get(i).addAdjacentSentence(this.get(j).getCurrentSentence(), matAdj[i][j]);
					matAdj[i][j] = 1;
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
		for (int i = 0; i < this.size(); i++) {
			for (int j = 0; j < this.size(); j++)
				str += df.format(matAdj[i][j]) + "\t";
			str += "\n";
		}
		return str;
	}
}
