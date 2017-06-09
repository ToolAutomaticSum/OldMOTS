package model.task.process.old.scoringMethod;

import java.util.HashMap;

import model.task.process.old.AbstractProcess;
import optimize.Individu;
import optimize.SupportADNException;
import textModeling.wordIndex.Index;

public abstract class AbstractScoringMethod extends Individu {

	protected AbstractProcess currentProcess;
	
	protected Index index;
	
	public AbstractScoringMethod(int id) throws SupportADNException {
		super(id);
	}
	
	public abstract AbstractScoringMethod makeCopy() throws Exception;
	
	protected void initCopy(AbstractScoringMethod p) {
		p.setCurrentProcess(currentProcess);
		p.setIndex(index);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;

	public void init(AbstractProcess currentProcess, Index dictionnary) throws Exception {
		this.currentProcess = currentProcess;
		this.index = dictionnary;
		
		if (dictionnary.isEmpty())
			throw new Exception("Dictionnary is empty !");
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

	/*public String getInputType() {
		return inputType;
	}*/

	public Index getIndex() {
		return index;
	}

	public void setIndex(Index dictionnary) {
		this.index = dictionnary;
	}
}
