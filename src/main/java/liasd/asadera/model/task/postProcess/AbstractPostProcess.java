package main.java.liasd.asadera.model.task.postProcess;

import main.java.liasd.asadera.model.task.AbstractTask;
import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.optimize.Individu;
import main.java.liasd.asadera.optimize.SupportADNException;

public abstract class AbstractPostProcess extends Individu implements AbstractTask {

	protected AbstractProcess currentProcess;

	public AbstractPostProcess(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public void process() throws Exception {
	}

	@Override
	public abstract void finish() throws Exception;

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
}
