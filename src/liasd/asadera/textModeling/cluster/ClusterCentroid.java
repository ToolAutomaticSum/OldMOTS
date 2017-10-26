package liasd.asadera.textModeling.cluster;

import java.util.Map;

import liasd.asadera.textModeling.SentenceModel;
import liasd.asadera.textModeling.wordIndex.Index;

public abstract class ClusterCentroid extends Cluster {

	protected Index clusterDictionnary;
	protected Index dictionnary;
	protected Map<Integer, String> hashMapWord;
	protected double thresholdCluster;
	protected int nbMaxWord;
	
	//private double[] centroid;
	
	public ClusterCentroid(int id, Index dictionnary, Map<Integer, String> hashMapWord, int nbMaxWord, double thresholdCluster) {
		super(id);
		this.dictionnary = dictionnary;
		clusterDictionnary = new Index(this.dictionnary.getNbDocument());
		this.hashMapWord = hashMapWord;
		
		//centroid = new double[dictionnary.size()];
		this.nbMaxWord = nbMaxWord;
		this.thresholdCluster = thresholdCluster;
	}
	
	public abstract void addSentence(SentenceModel sentence);

	/**
	 * Retourne le barycentre du cluster
	 * @return centroid, double[]
	 */
	public abstract double[] getCentroid();
	
	@Override
	public String toString() {
		String str = id + "\n";

		double[] centroid = new double[dictionnary.size()];
		centroid = this.getCentroid();
		
		for (int i = 0; i<centroid.length;i++) {
			if (centroid[i]>0)
				str += hashMapWord.get(i) + " : " + centroid[i] + "\n";
		}
		
		for (int i = 0;i<this.size();i++)
			str += this.get(i).getSentence() + "\n";
		return str;
	}

	public Index getClusterDictionnary() {
		return clusterDictionnary;
	}

	public void setClusterDictionnary(Index clusterDictionnary) {
		this.clusterDictionnary = clusterDictionnary;
	}
}
