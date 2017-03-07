package model.task.process.summarizeMethod;

import java.util.List;

import optimize.SupportADNException;
import textModeling.SentenceModel;

public class CSIS extends AbstractSummarizeMethod {

	public CSIS(int id) throws SupportADNException {
		super(id);
	}

	@Override
	public List<SentenceModel> calculateSummary() throws Exception {
		return null;
	}

}
