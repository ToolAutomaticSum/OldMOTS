package main.java.liasd.asadera.textModeling.cluster;

import java.util.Map;

import main.java.liasd.asadera.textModeling.SentenceModel;
import main.java.liasd.asadera.textModeling.wordIndex.Index;
import main.java.liasd.asadera.textModeling.wordIndex.WordIndex;

public abstract class ClusterCentroid<T extends WordIndex> extends Cluster {

	protected Index<T> clusterDictionnary;
	protected Index<T> dictionnary;
	protected Map<Integer, String> hashMapWord;
	protected double thresholdCluster;
	protected int nbMaxWord;

	public ClusterCentroid(int id, Index<T> dictionnary, Map<Integer, String> hashMapWord, int nbMaxWord,
			double thresholdCluster) {
		super(id);
		this.dictionnary = dictionnary;
		clusterDictionnary = new Index<T>(this.dictionnary.getNbDocument());
		this.hashMapWord = hashMapWord;
		this.nbMaxWord = nbMaxWord;
		this.thresholdCluster = thresholdCluster;
	}

	public abstract void addSentence(SentenceModel sentence);

	/**
	 * @return centroid, double[]
	 */
	public abstract double[] getCentroid();

	@Override
	public String toString() {
		String str = id + "\n";

		double[] centroid = new double[dictionnary.size()];
		centroid = this.getCentroid();

		for (int i = 0; i < centroid.length; i++) {
			if (centroid[i] > 0)
				str += hashMapWord.get(i) + " : " + centroid[i] + "\n";
		}

		for (int i = 0; i < this.size(); i++)
			str += this.get(i).getSentence() + "\n";
		return str;
	}

	public Index<T> getClusterDictionnary() {
		return clusterDictionnary;
	}

	public void setClusterDictionnary(Index<T> clusterDictionnary) {
		this.clusterDictionnary = clusterDictionnary;
	}
}
