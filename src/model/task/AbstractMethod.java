package model.task;

import model.SModel;
import textModeling.MultiCorpus;

public abstract class AbstractMethod {

	protected int id;
	protected SModel model;
	protected MultiCorpus currentMultiCorpus;

	public AbstractMethod(int id) {
		super();
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public SModel getModel() {
		return model;
	}

	public void setModel(SModel model) {
		this.model = model;
	}

	public MultiCorpus getCurrentMultiCorpus() {
		return currentMultiCorpus;
	}

	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus) {
		this.currentMultiCorpus = currentMultiCorpus;
	}
}
