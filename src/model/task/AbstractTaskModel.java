package model.task;

import model.SModel;

public interface AbstractTaskModel extends AbstractTask {

	public SModel getModel();

	public void setModel(SModel model);
}
