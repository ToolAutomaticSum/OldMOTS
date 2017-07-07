package model.task.process.scoringMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.task.process.AbstractProcess;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import optimize.SupportADNException;
import textModeling.Corpus;
import tools.PairSentenceScore;

public abstract class AbstractScoringMethod extends ParametrizedMethod implements ScoreBasedIn, ScoreBasedOut {

	protected AbstractProcess currentProcess;
	protected ArrayList<PairSentenceScore> sentencesScores;
	
	public AbstractScoringMethod(int id) throws SupportADNException {
		super(id);
		sentencesScores = new ArrayList<PairSentenceScore>();
		
		listParameterIn.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
		listParameterOut.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedOut.class));
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
	public abstract void computeScores(List<Corpus> listCorpus) throws Exception;

	public void finish() {
		sentencesScores.clear();
	}
	
	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}
	
	@Override
	public boolean isOutCompatible(ParametrizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn().contains(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
	}

	@Override
	public void setCompatibility(ParametrizedMethod compMethod) {
		((ScoreBasedIn)compMethod).setScore(sentencesScores);
	}

	@Override
	public ArrayList<PairSentenceScore> getScore() {
		return sentencesScores;
	}
	
	@Override
	public void setScore(ArrayList<PairSentenceScore> score) {
		this.sentencesScores = score;
	}
}
