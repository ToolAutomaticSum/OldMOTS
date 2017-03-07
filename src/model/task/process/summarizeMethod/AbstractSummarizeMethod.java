package model.task.process.summarizeMethod;

import java.util.List;

import model.task.process.AbstractProcess;
import optimize.Individu;
import optimize.SupportADNException;
import textModeling.SentenceModel;

public abstract class AbstractSummarizeMethod extends Individu {

	protected AbstractProcess currentProcess;
	
	public AbstractSummarizeMethod(int id) throws SupportADNException {
		super(id);
	}
	
	public abstract List<SentenceModel> calculateSummary() throws Exception;

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
		currentProcess.getSupportADN().putAll(supportADN);
	}
	
	public boolean isQueryBased() {
		return this.getClass() == VectorQueryBasedIn.class;
	}
}
