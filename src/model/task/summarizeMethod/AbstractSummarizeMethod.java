package model.task.summarizeMethod;

import java.util.List;

import model.task.AbstractMethod;
import model.task.process.AbstractProcess;
import textModeling.SentenceModel;

public abstract class AbstractSummarizeMethod extends AbstractMethod {

	protected AbstractProcess currentProcess;
	
	public AbstractSummarizeMethod(int id) {
		super(id);
	}
	
	public abstract List<SentenceModel> calculateSummary() throws Exception;

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
	
	public boolean isQueryBased() {
		return this.getClass() == VectorQueryBasedIn.class;
	}
}
