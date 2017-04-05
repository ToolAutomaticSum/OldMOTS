package model.task.process.summarizeMethod;

import java.util.List;

import optimize.SupportADNException;
import textModeling.SentenceModel;

/**
 * Comme MMR mais pas avec nombre de mot commun dans les phrases (type Jaccard similarity)
 * @author valnyz
 *
 */
public class CSIS extends AbstractSummarizeMethod {

	public CSIS(int id) throws SupportADNException {
		super(id);
	}
	
	@Override
	public void initADN() throws Exception {
	}

	@Override
	public List<SentenceModel> calculateSummary() throws Exception {
		return null;
	}

}
