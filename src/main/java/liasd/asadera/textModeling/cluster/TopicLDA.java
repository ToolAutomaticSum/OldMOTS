package main.java.liasd.asadera.textModeling.cluster;

import java.util.List;

import main.java.liasd.asadera.textModeling.SentenceModel;

public class TopicLDA extends Cluster implements Comparable<TopicLDA> {

	protected double scoreCorpus = 0;

	public TopicLDA(int id, double scoreCorpus) {
		super(id);
		this.id = id;
		this.scoreCorpus = scoreCorpus;
	}

	public TopicLDA(int id, List<SentenceModel> listSentenceModel, double scoreCorpus) {
		super(id, listSentenceModel);
		this.id = id;
		this.scoreCorpus = scoreCorpus;
	}

	public double getScoreCorpus() {
		return scoreCorpus;
	}

	public void setScoreCorpus(double scoreCorpus) {
		this.scoreCorpus = scoreCorpus;
	}

	@Override
	public int compareTo(TopicLDA o) {
		Double temp = (Double) scoreCorpus;
		return temp.compareTo(o.getScoreCorpus());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
