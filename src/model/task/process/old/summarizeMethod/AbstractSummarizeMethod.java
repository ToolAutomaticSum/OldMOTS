package model.task.process.old.summarizeMethod;

import java.util.HashMap;
import java.util.List;

import model.task.process.old.AbstractProcess;
import optimize.Individu;
import optimize.SupportADNException;
import textModeling.SentenceModel;

public abstract class AbstractSummarizeMethod extends Individu {

	protected AbstractProcess currentProcess;
	
	public AbstractSummarizeMethod(int id) throws SupportADNException {
		super(id);
	}
	
	public abstract AbstractSummarizeMethod makeCopy() throws Exception;
	
	protected void initCopy(AbstractSummarizeMethod p) {
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
}
