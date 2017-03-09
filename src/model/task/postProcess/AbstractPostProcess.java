package model.task.postProcess;

import model.SModel;
import model.task.AbstractTaskModel;
import model.task.process.AbstractProcess;
import optimize.Individu;
import optimize.SupportADNException;

public abstract class AbstractPostProcess extends Individu implements AbstractTaskModel {

	protected AbstractProcess currentProcess;
	protected SModel model;
	
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
	
	@Override
	public SModel getModel() {
		return model;
	}
	
	@Override
	public void setModel(SModel model) {
		this.model = model;
	}
}
