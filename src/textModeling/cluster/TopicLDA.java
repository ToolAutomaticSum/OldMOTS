package textModeling.cluster;

import java.util.List;

import textModeling.SentenceModel;

// idée ajouter score sur le corpus puis implements Comparable
// permettrai tri par score sur le corpus et itérer la dessus
public class TopicLDA extends Cluster implements Comparable<TopicLDA>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1747392675846228624L;
	protected double scoreCorpus = 0;

	public TopicLDA(int id, double scoreCorpus) {
		super(id/*, listSentenceTopic*/);
		this.id = id;
		//this.listSentenceTopic = listSentenceTopic;
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
