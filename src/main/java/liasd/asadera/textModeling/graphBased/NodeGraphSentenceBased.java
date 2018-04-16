package main.java.liasd.asadera.textModeling.graphBased;

import java.util.HashMap;
import java.util.Map;

import main.java.liasd.asadera.textModeling.SentenceModel;

public class NodeGraphSentenceBased {

	private SentenceModel currentSentence; // vertices (i.e. nodes)
	private int idNode;
	private Map<SentenceModel, Double> adjacentSentence = new HashMap<SentenceModel, Double>(); // edges (i.e. branch)

	public NodeGraphSentenceBased(int iD, SentenceModel currentSentence) {
		super();
		this.idNode = iD;
		this.currentSentence = currentSentence;
	}

	public void addAdjacentSentence(SentenceModel sentence, double weigth) {
		adjacentSentence.put(sentence, weigth);
	}

	public SentenceModel getCurrentSentence() {
		return currentSentence;
	}

	public void setCurrentSentence(SentenceModel currentSentence) {
		this.currentSentence = currentSentence;
	}

	public int getIdNode() {
		return idNode;
	}

	public void setIdNode(int idNode) {
		this.idNode = idNode;
	}

	public Map<SentenceModel, Double> getAdjacentSentence() {
		return adjacentSentence;
	}

	public void setAdjacentSentence(Map<SentenceModel, Double> adjacentSentence) {
		this.adjacentSentence = adjacentSentence;
	}
}
