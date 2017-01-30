package textModeling.wordIndex.LDA;

import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.WordIndex;

public class WordLDA extends WordIndex{
	
	private int K;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2881569573918395518L;
	private double[] topicDistribution;
	
	public WordLDA(String word, Dictionnary dictionnary, int iD, int nbTopic) {
		super(word, dictionnary, iD);
		this.K = nbTopic;
		this.topicDistribution = new double[K];
	}
	
	public WordLDA(String word, Dictionnary dictionnary, int iD, double[] topicDistribution) {
		super(word, dictionnary, iD);
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
