package model.task.process.scoringMethod;

import java.util.Map;

import model.task.process.AbstractProcess;
import optimize.Individu;
import optimize.SupportADNException;
import textModeling.wordIndex.Dictionnary;
import textModeling.wordIndex.WordIndex;

public abstract class AbstractScoringMethod extends Individu {

	protected AbstractProcess currentProcess;
	
	protected Dictionnary dictionnary;
	protected Map<Integer, String> hashMapWord;
	
	public AbstractScoringMethod(int id) throws SupportADNException {
		super(id);
	}

	public void init(AbstractProcess currentProcess, Dictionnary dictionnary, Map<Integer, String> hashMapWord) throws Exception {
		this.currentProcess = currentProcess;
		this.dictionnary = dictionnary;
		this.hashMapWord = hashMapWord;
		
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
		currentProcess.getSupportADN().putAll(supportADN);
	}

	/*public String getInputType() {
		return inputType;
	}*/

	public Map<String, WordIndex> getDictionnary() {
		return dictionnary;
	}

	public void setDictionnary(Dictionnary dictionnary) {
		this.dictionnary = dictionnary;
	}
}
