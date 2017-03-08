package textModeling.wordIndex.LDA;

import textModeling.wordIndex.Index;
import textModeling.wordIndex.WordIndex;

public class WordLDA extends WordIndex{
	
	private int K;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2881569573918395518L;
	private double[] topicDistribution;
	
	public WordLDA(String word, Index dictionnary, int nbTopic) {
		super(word, dictionnary);
		this.K = nbTopic;
		this.topicDistribution = new double[K];
	}
	
	public WordLDA(String word, Index dictionnary, double[] topicDistribution) {
		super(word, dictionnary);
		K = topicDistribution.length;
		this.topicDistribution = topicDistribution;
	}

	public double[] getTopicDistribution() {
		return topicDistribution;
	}

	public void setTopicDistribution(double[] topicDistribution) {
		this.topicDistribution = topicDistribution;
	}

	public int getK() {
		return K;
	}

	public void setK(int k) {
		K = k;
	}
}
