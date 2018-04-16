package main.java.liasd.asadera.model.task.preProcess;

import main.java.liasd.asadera.model.task.AbstractMethod;
import main.java.liasd.asadera.model.task.AbstractTask;

public abstract class AbstractPreProcess extends AbstractMethod implements AbstractTask {

	private AbstractPreProcess currentProcess;

	public AbstractPreProcess(int id) {
		super(id);
	}

	public AbstractPreProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractPreProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
}
