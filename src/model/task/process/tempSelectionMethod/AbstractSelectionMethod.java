package model.task.process.tempSelectionMethod;

import java.util.HashMap;
import java.util.List;

import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.tempProcess.AbstractProcess;
import optimize.SupportADNException;
import textModeling.SentenceModel;

public abstract class AbstractSelectionMethod extends ParametrizedMethod {

	protected AbstractProcess currentProcess;

	public AbstractSelectionMethod(int id) throws SupportADNException {
		super(id);
	}

	public abstract AbstractSelectionMethod makeCopy() throws Exception;
	
	protected void initCopy(AbstractSelectionMethod p) {
		p.setCurrentProcess(currentProcess);
		if (supportADN != null)
			p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;
	
	public abstract List<SentenceModel> calculateSummary() throws Exception;

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
	
	@Override
	public abstract boolean isOutCompatible(ParametrizedMethod compatibleMethod);

	@Override
	public abstract void setCompatibility(ParametrizedMethod compMethod);
}
