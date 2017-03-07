package textModeling.cluster;

import java.util.Map;

import textModeling.SentenceModel;
import textModeling.wordIndex.Dictionnary;

public abstract class ClusterCentroid extends Cluster {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6680832604482340969L;
	
	protected Dictionnary clusterDictionnary;
	protected Dictionnary dictionnary;
	protected Map<Integer, String> hashMapWord;
	protected double thresholdCluster;
	protected int nbMaxWord;
	
	//private double[] centroid;
	
	public ClusterCentroid(int id, Dictionnary dictionnary, Map<Integer, String> hashMapWord, int nbMaxWord, double thresholdCluster) {
		super(id);
		this.dictionnary = dictionnary;
		clusterDictionnary = new Dictionnary(this.dictionnary.getNbDocument());
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

	public Dictionnary getClusterDictionnary() {
		return clusterDictionnary;
	}

	public void setClusterDictionnary(Dictionnary clusterDictionnary) {
		this.clusterDictionnary = clusterDictionnary;
	}
}
