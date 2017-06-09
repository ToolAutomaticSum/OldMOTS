package model.task.process.comparativeMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.task.process.AbstractProcess;
import model.task.process.processCompatibility.ParametrizedMethod;
import model.task.process.processCompatibility.ParametrizedType;
import model.task.process.scoringMethod.ScoreBasedIn;
import model.task.process.scoringMethod.ScoreBasedOut;
import optimize.SupportADNException;
import textModeling.Corpus;
import textModeling.SentenceModel;
import tools.Pair;
import tools.PairSentenceScore;

public abstract class AbstractComparativeMethod extends ParametrizedMethod {

	protected AbstractProcess currentProcess;

	public AbstractComparativeMethod(int id) throws SupportADNException {
		super(id);
		
		listParameterIn.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
		listParameterOut.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedOut.class));
	}

	public abstract AbstractComparativeMethod makeCopy() throws Exception;
	
	protected void initCopy(AbstractComparativeMethod p) {
		p.setCurrentProcess(currentProcess);
		if (supportADN != null)
			p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;
	
	public abstract List<Pair<SentenceModel, String>> calculateDifference(List<Corpus> listCorpus) throws Exception;

	public abstract void finish();
	
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
