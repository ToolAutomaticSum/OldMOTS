package model.task.summarizeMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exception.VectorDimensionException;
import textModeling.SentenceModel;
import textModeling.cluster.TopicLDA;
import tools.Tools;

public class LinkedSentenceLDA extends AbstractSummarizeMethod implements TopicLdaBasedIn, VectorQueryBasedIn {

	private double[] query;
	private List<TopicLDA> listTopic;
	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	
	public LinkedSentenceLDA(int id) {
		super(id);
	}	
	
	/**
	 * LinkedSentence not really, we choose the most similar sentence with averageVector in the (limitSentenceByTopic) first sentence of each non void Topic
	 * @param averageVector
	 * @param listTopic
	 * @param sizeSummary
	 * @param limitSentenceByTopic
	 * @return
	 * @throws VectorDimensionException 
	 */
	public static List<SentenceModel> generateLinkedSentenceLDASummary(Map<SentenceModel, double[]> sentenceCaracteristic, double[] averageVector, List<TopicLDA> listTopic, int sizeSummary, int limitSentenceByTopic) throws VectorDimensionException {
		//Nb topic non nulle
		int nbNonVoidTopic = 0;
		for (int i = 0; i < listTopic.size(); i++) {
			if (listTopic.get(i).getScoreCorpus() != 0) {
				//Tri des phrases des Topics non vide par rapport à leur score sur ce topic
				SentenceComparator comparator = new SentenceComparator();
				comparator.setVectorDimensionToCompare(listTopic.get(i).getId());
				Collections.sort(listTopic.get(i), comparator);
				Collections.reverse(listTopic.get(i));
				nbNonVoidTopic++;
			}
		}	
		
		List<SentenceModel> summary = new ArrayList<SentenceModel>();

		// Détermination des phrases suivantes
		int currentTopic = 0;
		int currentNbSentenceInSummary = 0;
		while (currentNbSentenceInSummary <= sizeSummary) {
			Iterator<SentenceModel >senIt = listTopic.get(currentTopic).iterator();
			int j = 0; //currentSentence
			int k = 0; //Comparaison sur les X meilleurs phrases (k<X)
			int bestSen = 0;
			double distanceBestSentence = 0;
			while (k<limitSentenceByTopic && senIt.hasNext()) {
				SentenceModel secondSen = (SentenceModel) senIt.next();
				if (!summary.contains(secondSen)) {
					double temp = Tools.objectiveFunction(sentenceCaracteristic.get(secondSen), averageVector);
					if (Math.abs(temp) > Math.abs(distanceBestSentence)) {
						bestSen = j;
						distanceBestSentence = temp;
					}
					System.out.println(secondSen.getiD() + " " + distanceBestSentence + " " + temp);
					k++;
				}
				j++;
			}
			System.out.println(bestSen);
			summary.add(listTopic.get(currentTopic).get(bestSen));
			currentNbSentenceInSummary++;
			if (currentTopic+1 == nbNonVoidTopic) { //Si nombre de topic non nulle < nombre de phrase dans le résumé, il faut prendre plusieurs fois des phrases dans le même topic
				currentTopic = 0;
				limitSentenceByTopic++;
			}
			else
				currentTopic++;
		}
		return summary;
			
		/** Version 2*/
		/**
		 * Voir ParagraphViaLinkedSentence => generateLinkedSentenceLDASummary();
		 */
	}
	
	/** Version 1*/
	/*@Override
	public void process() {
		List<SentenceModel> summary = new ArrayList<SentenceModel>();
		SentenceModel firstSen = (SentenceModel) listTopic.get(0).getListSentenceTopic().get(0).first;
		summary.add(firstSen);
		for (int i = 1; i<listTopic.size(); i++) {
			Iterator<Pair> senIt = listTopic.get(i).getListSentenceTopic().iterator();
			int j = 0;
			int k = 0; //Comparaiso sur les 5 meilleurs phrases (k<6)
			int bestSen = 0;
			double distanceBestSentence = 1000.0;
				while (k<51 && senIt.hasNext()) {
					SentenceModel secondSen = (SentenceModel) senIt.next().first;
					if (!summary.contains(secondSen)) {
						double temp = Tools.tanimotoDistance((double[]) firstSen.getScore(), (double[]) secondSen.getScore());
						if (Math.abs(1.0-temp) < Math.abs(1.0-distanceBestSentence)) {
							bestSen = j;
							distanceBestSentence = temp;
						}
						System.out.println(firstSen.getiD() + " " + secondSen.getiD() + " " + distanceBestSentence + " " + temp);
						k++;
					}
					j++;
				}
				System.out.println(bestSen);
		
			firstSen = (SentenceModel) listTopic.get(i).getListSentenceTopic().get(bestSen).first;
			summary.add(firstSen);
		}
		
		getCurrentProcess().setSummary(summary);
		
	}*/
		
	/*public double[] resizeVector(double[] v, int size) {
		if (v.length > size) {
			double[] a = new double[size];
			
			for (int i = 0; i<size; i++)
				a[i] = v[i];
			return a;
		}
		else
			return v;
	}*/
	
	public static class SentenceComparator implements Comparator<SentenceModel> {
		private int vectorDimensionToCompare = 0;
		private Map<SentenceModel, double[]> sentenceCaracteristic;
		@Override
		public int compare(SentenceModel o1, SentenceModel o2) {
			double[] s1 = sentenceCaracteristic.get(o1);
			double[] s2 = sentenceCaracteristic.get(o2);
			return (int) Math.signum(s1[vectorDimensionToCompare] - s2[vectorDimensionToCompare]);
		}

		public int getVectorDimensionToCompare() {
			return vectorDimensionToCompare;
		}

		public void setVectorDimensionToCompare(int vectorDimensionToCompare) {
			this.vectorDimensionToCompare = vectorDimensionToCompare;
		}

		public Map<SentenceModel, double[]> getSentenceCaracteristic() {
			return sentenceCaracteristic;
		}

		public void setSentenceCaracteristic(Map<SentenceModel, double[]> sentenceCaracteristic) {
			this.sentenceCaracteristic = sentenceCaracteristic;
		}
	}

	@Override
	public List<SentenceModel> calculateSummary() throws Exception {
		//int sizeSummary = 8;
		int limitSentenceByTopic = 10;

		return generateLinkedSentenceLDASummary(sentenceCaracteristic, query, listTopic, getCurrentProcess().getSizeSummary(), limitSentenceByTopic);
	}

	@Override
	public void setVectorQuery(double[] vectorQuery) {
		query = vectorQuery;
	}

	@Override
	public void setListTopicLda(List<TopicLDA> listTopic) {
		this.listTopic = listTopic;
	}

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;		
	}
	
	/**
	 * Comparaison de SentenceModel avec Score as double[]
	 * @author Val
	 *
	 */
	/*public class SentenceComparator implements Comparator<SentenceModel> {
		
		int vectorSize = -1;
		int tempVectorSize = -1;

		@Override
		public int compare(SentenceModel o1, SentenceModel o2) {
			tempVectorSize = vectorSize;
			return compare((double[])o1.getScore(), (double[])o2.getScore());
		}
		
		public int compare(double o1, double o2) {
			return (int) Math.signum(o1 - o2);
		}
		
		public int compare(double[] o1, double[] o2) {
			int c = compare(o1[tempVectorSize-1], o2[tempVectorSize-1]);
			if (c == 0) {
				if (tempVectorSize > 1) {
					tempVectorSize--;
					return compare(resizeVector(o1, tempVectorSize),resizeVector(o2, tempVectorSize));
				}
				else
					return 0;
			}
			else
				return c;
		}

		public int getVectorSize() {
			return vectorSize;
		}

		public void setVectorSize(int vectorSize) {
			this.tempVectorSize = vectorSize;
			this.vectorSize = vectorSize;
		}
	}*/
	
}
