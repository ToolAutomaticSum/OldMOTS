package model.task.postProcess;

import model.task.AbstractTask;
import model.task.process.AbstractProcess;

public abstract class AbstractPostProcess extends AbstractTask {

	protected AbstractProcess currentProcess;
	
	public AbstractPostProcess(int id) {
		super(id);	
	}
	
	@Override
	public void process() throws Exception {		
	}
	
	@Override
	public abstract void finish();

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
}
