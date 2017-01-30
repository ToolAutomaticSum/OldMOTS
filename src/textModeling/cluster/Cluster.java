package textModeling.cluster;

import java.util.ArrayList;
import java.util.List;

import textModeling.SentenceModel;

public abstract class Cluster extends ArrayList<SentenceModel> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3139753760417744467L;
	protected int id;
	//protected List<SentenceModel> listSentenceTopic = new ArrayList<SentenceModel>();
	
	public Cluster(int id/*, List<SentenceModel> listSentenceTopic*/) {
		super();
		this.id = id;
		//this.listSentenceTopic = listSentenceTopic;
	}
	
	public Cluster(int id, List<SentenceModel> listSentenceTopic) {
		super(listSentenceTopic);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	/*public List<SentenceModel> getListSentenceTopic() {
		return listSentenceTopic;
	}

	public void setListSentenceTopic(List<SentenceModel> listSentenceTopic) {
		this.listSentenceTopic = listSentenceTopic;
	}*/
}
