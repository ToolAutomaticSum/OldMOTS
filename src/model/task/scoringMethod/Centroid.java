package model.task.scoringMethod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import exception.VectorDimensionException;
import model.task.VectorCaracteristicBasedIn;
import model.task.VectorCaracteristicBasedOut;
import model.task.process.AbstractProcess;
import model.task.process.LDA.LdaBasedOut;
import textModeling.SentenceModel;
import textModeling.cluster.ClusterCentroid;
import textModeling.cluster.ClusterCentroid_TF_IDF;
import textModeling.wordIndex.Dictionnary;
import tools.vector.ToolsVector;

/**
 * @author Val
 *
 */
public abstract class Centroid extends AbstractScoringMethod implements VectorCaracteristicBasedIn, VectorCaracteristicBasedOut {

	protected String clusterType = "TF_IDF";
	
	protected double thresholdCluster;
	protected int nbMaxWordInCluster;
	
	private double threshold;
	private int idCluster;
	protected Map<Integer, ClusterCentroid> listCluster = new HashMap<Integer, ClusterCentroid>();
	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	
	public Centroid(int id) throws Exception {
		super(id);
	}
	
	@Override
	public void init(AbstractProcess currentProcess, Dictionnary dictionnary, Map<Integer, String> hashMapWord) throws Exception {
		super.init(currentProcess, dictionnary, hashMapWord);
		
		for (Class<?> c : currentProcess.getClass().getInterfaces()) {
			if (c.equals(LdaBasedOut.class))
				clusterType = "LDA";
		}
		
		threshold = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "Threshold"));
		nbMaxWordInCluster = Integer.parseInt(getCurrentProcess().getModel().getProcessOption(id, "NbMaxWordInCluster"));
		thresholdCluster = Double.parseDouble(getCurrentProcess().getModel().getProcessOption(id, "ThresholdCluster"));
	
		/** Construction des différents clusters */
		idCluster = 0;
		//int size = sentenceCaracteristic.size();
		Iterator<SentenceModel> senIt = sentenceCaracteristic.keySet().iterator();
		if (senIt.hasNext()) {
			/** Création du cluster de la première phrase */
			ClusterCentroid cluster = instanciateCluster(clusterType);
				
			SentenceModel sen = senIt.next();
			cluster.addSentence(sen);
			listCluster.put(idCluster, cluster);
			//senIt.remove();
			//size = sentenceCaracteristic.size();
			/** Ajout des phrases suivantes dans les clusters */
			while(senIt.hasNext()) {
				addNextSentence(senIt.next());
				//senIt.remove();
				System.out.println(listCluster.size());
			}
		}
		
		//System.out.println(listCluster);
	}

	@Override
	public abstract void computeScores() throws Exception;
	
	private void addNextSentence(SentenceModel sentence) throws VectorDimensionException {
		/** Tester similarité avec cluster existant */
		int result = findCluster(sentenceCaracteristic.get(sentence));
		if (result == -1) {
			ClusterCentroid cluster = instanciateCluster(clusterType);

			cluster.addSentence(sentence);
			idCluster++;
			listCluster.put(idCluster, cluster);
		}
		else {
			listCluster.get(result).addSentence(sentence);
		}
	}
	
	private int findCluster(double[] caracSentence) throws VectorDimensionException {
		double maxSim = 0;
		int maxSimClusterId = -1;
		Iterator<ClusterCentroid> itCluster = listCluster.values().iterator();
		while (itCluster.hasNext()) {
			ClusterCentroid cluster = itCluster.next();
			double sim = ToolsVector.cosineSimilarity(cluster.getCentroid(), caracSentence); //cluster.cosineSimilarity(caracSentence);
 			if (sim > threshold && sim > maxSim) {
				//System.out.println(sim);
				maxSim = sim;
				maxSimClusterId = cluster.getId();
			}
		}
		
		return maxSimClusterId;
	}
	
	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}

	@Override
	public Map<SentenceModel, double[]> getVectorCaracterisic() {
		return sentenceCaracteristic;
	}
	
	protected ClusterCentroid instanciateCluster(String clusterType) throws NullPointerException {
		ClusterCentroid cluster;
		if (clusterType.equals("TF_IDF"))
			cluster = new ClusterCentroid_TF_IDF(idCluster, dictionnary, hashMapWord, nbMaxWordInCluster, thresholdCluster);
		else
			throw new NullPointerException("Process need to be TF_IDF.");
		return cluster;
	}
}
