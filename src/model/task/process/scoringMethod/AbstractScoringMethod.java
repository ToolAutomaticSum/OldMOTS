package model.task.process.tempScoringMethod;

import java.util.HashMap;

import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.tempProcess.AbstractProcess;
import optimize.SupportADNException;

public abstract class AbstractScoringMethod extends ParametrizedMethod {

	protected AbstractProcess currentProcess;
	
	public AbstractScoringMethod(int id) throws SupportADNException {
		super(id);
	}

	public abstract AbstractScoringMethod makeCopy() throws Exception;
	
	protected void initCopy(AbstractScoringMethod p) {
		p.setCurrentProcess(currentProcess);
		//p.setIndex(index);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;

	public void init(AbstractProcess currentProcess) throws Exception {
		this.currentProcess = currentProcess;
	}
	
	/**
	 * 
	 * When implementing this method, don't forget to set this.areSentencesScored to true
	 * when scoring is complete. Otherwise, sortByScore() will throw a SentencesNotScoredException.
	 * @return 
	 * @throws Exception 
	 */
	public abstract void computeScores() throws Exception;
	
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
