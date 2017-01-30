package model.task.preProcess;

import model.task.AbstractTask;
import model.task.process.AbstractProcess;

public abstract class AbstractPreProcess extends AbstractTask {
	
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
