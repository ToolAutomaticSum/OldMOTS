package main.java.liasd.asadera.model.task.process.scoringMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.liasd.asadera.model.task.process.AbstractProcess;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedMethod;
import main.java.liasd.asadera.model.task.process.processCompatibility.ParameterizedType;
import main.java.liasd.asadera.optimize.SupportADNException;
import main.java.liasd.asadera.textModeling.Corpus;
import main.java.liasd.asadera.textModeling.SentenceModel;

public abstract class AbstractScoringMethod extends ParameterizedMethod implements ScoreBasedIn, ScoreBasedOut {

	protected AbstractProcess currentProcess;
	protected Map<SentenceModel, Double> sentencesScore;

	public AbstractScoringMethod(int id) throws SupportADNException {
		super(id);

		listParameterIn.add(new ParameterizedType(Double.class, Map.class, ScoreBasedIn.class));
		listParameterOut.add(new ParameterizedType(Double.class, Map.class, ScoreBasedOut.class));
	}

	public abstract AbstractScoringMethod makeCopy() throws Exception;

	protected void initCopy(AbstractScoringMethod p) {
		p.setCurrentProcess(currentProcess);
		p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}

	public void initADN() throws Exception {
		sentencesScore = new HashMap<SentenceModel, Double>();
	}

	public void init(AbstractProcess currentProcess) throws Exception {
		this.currentProcess = currentProcess;
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract void computeScores(List<Corpus> listCorpus) throws Exception;

	public void finish() {
		sentencesScore.clear();
	}

	public AbstractProcess getCurrentProcess() {
		return currentProcess;
	}

	public void setCurrentProcess(AbstractProcess currentProcess) {
		this.currentProcess = currentProcess;
	}

	@Override
	public boolean isOutCompatible(ParameterizedMethod compatibleMethod) {
		return compatibleMethod.getParameterTypeIn()
				.contains(new ParameterizedType(Double.class, Map.class, ScoreBasedIn.class));
	}

	@Override
	public void setCompatibility(ParameterizedMethod compMethod) {
		((ScoreBasedIn) compMethod).setScore(sentencesScore);
	}

	@Override
	public Map<SentenceModel, Double> getScore() {
		return sentencesScore;
	}

	@Override
	public void setScore(Map<SentenceModel, Double> score) {
		this.sentencesScore = score;
	}
}
