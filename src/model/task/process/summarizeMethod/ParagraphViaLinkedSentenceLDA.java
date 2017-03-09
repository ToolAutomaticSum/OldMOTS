package model.task.process.summarizeMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import exception.VectorDimensionException;
import jgibblda.Pair;
import model.task.process.summarizeMethod.LinkedSentenceLDA.SentenceComparator;
import optimize.SupportADNException;
import textModeling.SentenceModel;
import textModeling.cluster.TopicLDA;
import tools.Tools;

public class ParagraphViaLinkedSentenceLDA extends AbstractSummarizeMethod implements TopicLdaBasedIn {
	private int nbParagraph = 50;
	private int sizeParagraph = 4;

	protected Map<SentenceModel, double[]> sentenceCaracteristic;
	
	private List<TopicLDA> listTopic;
	int bestTopic;
	int K;
	
	private double redundancyThreshold = 0.7;
	private boolean testRedundant = false;
	
	protected double[] averageVector;
	
	public ParagraphViaLinkedSentenceLDA(int id) throws SupportADNException {
		super(id);
	}
	
	public void process() throws Exception {
		List<Pair> listParagraphScore = new ArrayList<Pair>();
		List<List<SentenceModel>> listGeneratedParagraph = new ArrayList<List<SentenceModel>>();
		for (int i = 0; i < nbParagraph; i++) {
			listGeneratedParagraph.add(generateLinkedSentenceLDASummary(sentenceCaracteristic, averageVector, listTopic, sizeParagraph, 50));
			listParagraphScore.add(new Pair(i, calculateParagraphScore(sentenceCaracteristic, averageVector, listGeneratedParagraph.get(i))));
		}

		Collections.sort(listParagraphScore);	
		
		List<SentenceModel> summary = new ArrayList<SentenceModel>();
		if (testRedundant) {
			//On ajoute le r�sum� qui est le plus proche du document puis on ajoute le suivant en testant si la redondance est inf�rieur � 
			//getCurrentProcess().setSummary(listGeneratedParagraph.get((int)listParagraphScore.get(0).first));
			
			summary.addAll(listGeneratedParagraph.get((int)(listParagraphScore.get(0).first)));
			int i = 1;
			boolean notAdd = true;
			while (notAdd && i < listParagraphScore.size() && getCurrentProcess().getSummary().size() < getCurrentProcess().getSizeSummary()) {
				boolean notRedundant = true;
				int j = 0;
				while (notRedundant && j < getCurrentProcess().getSummary().size()/sizeParagraph) {
					//Construction du vecteur moyen des documents par topic
					double[] testedSentenceScore = new double[K];
					for (int k = 0; k < listGeneratedParagraph.get((int)listParagraphScore.get(i).first).size(); k++) {
						for (int l = 0; l < K; l++)
							testedSentenceScore[l] += (sentenceCaracteristic.get((listGeneratedParagraph.get((int)listParagraphScore.get(i).first)).get(k)))[l];
						for (int l = 0; l < K; l++)
							testedSentenceScore[l] /= listGeneratedParagraph.get((int)listParagraphScore.get(i).first).size();
					}
					double[] summarySentenceScore = new double[K]; //= (double[])getCurrentProcess().getSummary().get(j).getScore();
					for (int k = (j*sizeParagraph); k < ((j+1)*sizeParagraph); k++) {
						for (int l = 0; l < K; l++)
							summarySentenceScore[l] += (sentenceCaracteristic.get(getCurrentProcess().getSummary().get(k)))[l];
						for (int l = 0; l < K; l++)
							summarySentenceScore[l] /= sizeParagraph;
					}
					double score = Tools.objectiveFunction(testedSentenceScore, summarySentenceScore);
					if (score > redundancyThreshold) {
						if ((double)listParagraphScore.get(i).second > Tools.objectiveFunction(summarySentenceScore, averageVector)) {
							for (int m = 0; m<sizeParagraph;m++)
								getCurrentProcess().getSummary().remove(j*sizeParagraph + m);
							summary.addAll(listGeneratedParagraph.get((int)listParagraphScore.get(i).first));
						}
						notRedundant = false;
					}
					j++;
				}
				if (notRedundant) {
					summary.addAll(listGeneratedParagraph.get((int)listParagraphScore.get(i).first));
				}
				i++;
			}
		} else {
			summary.addAll(listGeneratedParagraph.get((int)listParagraphScore.get(0).first));
			summary.addAll(listGeneratedParagraph.get((int)listParagraphScore.get(1).first));
		}
		//getCurrentProcess().getSummary().add(summary);
	}
	
	/**
	 * First sentence is the most similar sentence with averageVector for the best topic, next sentence is the most similar with the precedent sentence in the next Topic
	 * Remove each sentence from listSentence in listTopic
	 * @param averageVector
	 * @param listTopic
	 * @param sizeSummary
	 * @param limitSentenceByTopic
	 * @return
	 * @throws VectorDimensionException 
	 */
	public static List<SentenceModel> generateLinkedSentenceLDASummary(Map<SentenceModel, double[]> sentenceCaracteristic, double[] averageVector, List<TopicLDA> listTopic, int sizeSummary, int limitSentenceByTopic) throws VectorDimensionException {
		List<SentenceModel> paragraph = new ArrayList<SentenceModel>();
		
		//Nb topic non nulle
		int nbNonVoidTopic = 0;
		for (int i = 0; i < listTopic.size(); i++) {
			if (listTopic.get(i).getScoreCorpus() != 0) {
				//Tri des phrases des Topics non vide par rapport � leur score sur ce topic
				SentenceComparator comparator = new SentenceComparator();
				comparator.setVectorDimensionToCompare(listTopic.get(i).getId());
				Collections.sort(listTopic.get(i), comparator);
				Collections.reverse(listTopic.get(i));
				nbNonVoidTopic++;
			}
		}
		
		/* Premi�re �tape Version 2 */
		//Choose first sentence via best tanimoto distance with averageDocumentsVector
		Iterator<SentenceModel> senIt = listTopic.get(0).iterator();
		int k = 0; //Comparaison sur les X meilleurs phrases (k<X)
		int bestSen = 0;
		double distanceBestSentence = 0.0;
		while (k<limitSentenceByTopic && senIt.hasNext()) {
			SentenceModel sen = (SentenceModel) senIt.next();
			double temp = Tools.objectiveFunction(averageVector, sentenceCaracteristic.get(sen));
			if (Math.abs(temp) > Math.abs(distanceBestSentence)) { //Tanimoto distance = valeur la plus proche de 1 possible
				bestSen = k;
				distanceBestSentence = temp;
			}
			k++;
		}
		SentenceModel firstSen = (SentenceModel) listTopic.get(0).get(bestSen);
		paragraph.add(firstSen);
		//firstSen.setScore(Tools./*cosineSimilarity*/objectiveFunction(averageVector, (double[]) firstSen.getScore()));
		removeSentenceListTopic(firstSen, listTopic);
		
		// D�termination des phrases suivantes
		int currentTopic = 1;
		int currentNbSentenceInSummary = 1;
		while (currentNbSentenceInSummary < sizeSummary) {
			senIt = listTopic.get(currentTopic).iterator();
			int j = 0; //currentSentence
			k = 0; //Comparaison sur les X meilleurs phrases (k<X) du topic currentTopic
			bestSen = 0;
			distanceBestSentence = 0;
			while (k<limitSentenceByTopic && senIt.hasNext()) {
				SentenceModel secondSen = (SentenceModel) senIt.next();
				if (!paragraph.contains(secondSen)) {
					double temp = Tools.objectiveFunction(sentenceCaracteristic.get(firstSen), sentenceCaracteristic.get(secondSen));
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
			firstSen = listTopic.get(currentTopic).get(bestSen);
			//firstSen.setScore(Tools./*cosineSimilarity*/objectiveFunction(averageVector, (double[]) firstSen.getScore()));
			paragraph.add(firstSen);
			removeSentenceListTopic(firstSen, listTopic);
			
			currentNbSentenceInSummary++;
			if (currentTopic+1 == nbNonVoidTopic) { //Si nombre de topic non nulle < nombre de phrase dans le r�sum�, il faut prendre plusieurs fois des phrases dans le m�me topic
				currentTopic = 0;
				limitSentenceByTopic++;
			}
			else
				currentTopic++;
		}
		
		return paragraph;
	}
	
	private static void removeSentenceListTopic(SentenceModel sen, List<TopicLDA> listTopic) {
		Iterator<TopicLDA> it = listTopic.iterator();
		while (it.hasNext()) {
			it.next().remove(sen);
		}
	}
	
	public static double calculateParagraphScore(Map<SentenceModel, double[]> sentenceCaracteristic, double[] averageVector, List<SentenceModel> listSentence) throws VectorDimensionException {
		double score = 0;
		
		if (listSentence.size() > 0) {
			for (int i = 0; i<listSentence.size(); i++) {
				score += Tools.objectiveFunction(averageVector, sentenceCaracteristic.get(listSentence.get(i)));
			}
			
			return score / listSentence.size();
		}
		else
			return score;
	}

	@Override
	public void setListTopicLda(List<TopicLDA> listTopic) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<SentenceModel> calculateSummary() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVectorCaracterisic(Map<SentenceModel, double[]> sentenceCaracteristic) {
		this.sentenceCaracteristic = sentenceCaracteristic;
	}


}
