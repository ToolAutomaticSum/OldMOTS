package model.task.preProcess;

import model.task.AbstractMethod;
import model.task.AbstractTask;
import model.task.process.old.AbstractProcess;

public abstract class AbstractPreProcess extends AbstractMethod implements AbstractTask {
	
	protected AbstractProcess currentProcess;
	
	public AbstractPreProcess(int id) {
		super(id);	
	}

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
}
