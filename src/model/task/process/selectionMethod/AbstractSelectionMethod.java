package model.task.process.selectionMethod;

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
import tools.PairSentenceScore;

public abstract class AbstractSelectionMethod extends ParametrizedMethod {

	protected AbstractProcess currentProcess;

	public AbstractSelectionMethod(int id) throws SupportADNException {
		super(id);
		
		listParameterIn.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedIn.class));
		listParameterOut.add(new ParametrizedType(PairSentenceScore.class, ArrayList.class, ScoreBasedOut.class));
	}

	public abstract AbstractSelectionMethod makeCopy() throws Exception;
	
	protected void initCopy(AbstractSelectionMethod p) {
		p.setCurrentProcess(currentProcess);
		if (supportADN != null)
			p.setSupportADN(new HashMap<String, Class<?>>(supportADN));
		p.setModel(model);
	}
	
	public abstract void initADN() throws Exception;
	
	public abstract List<SentenceModel> calculateSummary(List<Corpus> listCorpus) throws Exception;

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
