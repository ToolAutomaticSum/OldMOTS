package model.task;

import model.Model;

public abstract class AbstractMethod {

	protected int id;
	protected Model model;

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
	
	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}
}
