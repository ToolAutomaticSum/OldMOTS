package main.java.liasd.asadera.model.task;

import main.java.liasd.asadera.model.AbstractModel;
import main.java.liasd.asadera.textModeling.MultiCorpus;

public abstract class AbstractMethod {

	protected int id;
	protected AbstractModel model;
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

	public AbstractModel getModel() {
		return model;
	}

	public void setModel(AbstractModel model) {
		this.model = model;
	}

	public MultiCorpus getCurrentMultiCorpus() {
		return currentMultiCorpus;
	}

	public void setCurrentMultiCorpus(MultiCorpus currentMultiCorpus) {
		this.currentMultiCorpus = currentMultiCorpus;
	}
}
