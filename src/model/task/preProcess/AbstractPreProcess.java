package model.task.preProcess;

import model.SModel;
import model.task.AbstractMethod;
import model.task.AbstractTaskModel;
import model.task.process.AbstractProcess;

public abstract class AbstractPreProcess extends AbstractMethod implements AbstractTaskModel {
	
	protected AbstractProcess currentProcess;
	protected SModel model;
	
	public AbstractPreProcess(int id) {
		super(id);	
	}

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}

	@Override
	public SModel getModel() {
		return model;
	}
	
	@Override
	public void setModel(SModel model) {
		this.model = model;
	}
}
