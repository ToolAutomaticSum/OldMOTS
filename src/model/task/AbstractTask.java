package model.task;

import model.Model;

public abstract class AbstractTask extends AbstractMethod {

	protected Model model;
	
	public AbstractTask(int id) {
		super(id);
	}

	public abstract void init() throws Exception;
	
	public abstract void process() throws Exception;

	public abstract void finish() throws Exception;

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
}
