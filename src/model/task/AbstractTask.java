package model.task;

public interface AbstractTask {

	/*protected Model model;
	
	public AbstractTask(int id) {
		super(id);
	}*/

	public abstract void init() throws Exception;
	
	public abstract void process() throws Exception;

	public abstract void finish() throws Exception;
}
