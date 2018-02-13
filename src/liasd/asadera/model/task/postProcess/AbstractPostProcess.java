package liasd.asadera.model.task.postProcess;

import liasd.asadera.model.task.AbstractTask;
import liasd.asadera.model.task.process.AbstractProcess;
import liasd.asadera.optimize.Individu;
import liasd.asadera.optimize.SupportADNException;

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
