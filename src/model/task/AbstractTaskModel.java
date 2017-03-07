package model.task;

import model.Model;

public interface AbstractTaskModel extends AbstractTask {

	public Model getModel();

	public void setModel(Model model);
}
