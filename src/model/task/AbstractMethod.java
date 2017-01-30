package model.task;

public abstract class AbstractMethod {

	protected int id;

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
}
